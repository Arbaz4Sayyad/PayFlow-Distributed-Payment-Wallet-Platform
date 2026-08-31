# ============================================================
# PayFlow AWS Production Terraform Infrastructure as Code
# Region: us-east-1 (Multi-AZ: us-east-1a, us-east-1b, us-east-1c)
# ============================================================

terraform {
  required_version = ">= 1.7.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.40"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.27"
    }
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      Project     = "PayFlow"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# ─────────────── 1. Networking (VPC & Subnets) ───────────────

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.7.0"

  name = "payflow-production-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["${var.aws_region}a", "${var.aws_region}b", "${var.aws_region}c"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"] # EKS Workloads & DBs
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24", "10.0.103.0/24"] # ALBs & NAT Gateways
  database_subnets= ["10.0.201.0/24", "10.0.202.0/24", "10.0.203.0/24"] # Isolated DB Subnets

  enable_nat_gateway     = true
  single_nat_gateway     = false # Multi-AZ HA: 1 NAT GW per AZ
  one_nat_gateway_per_az = true
  enable_dns_hostnames   = true
  enable_dns_support     = true

  public_subnet_tags = {
    "kubernetes.io/role/elb" = 1
  }
  private_subnet_tags = {
    "kubernetes.io/role/internal-elb" = 1
  }
}

# ─────────────── 2. Kubernetes Cluster (AWS EKS) ───────────────

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "20.8.4"

  cluster_name    = "payflow-${var.environment}-eks"
  cluster_version = "1.29"

  cluster_endpoint_public_access = true
  vpc_id                         = module.vpc.vpc_id
  subnet_ids                     = module.vpc.private_subnets

  eks_managed_node_groups = {
    general_workloads = {
      name         = "payflow-general-ng"
      min_size     = 3
      max_size     = 20
      desired_size = 6

      instance_types = ["c6i.xlarge"] # 4 vCPU, 8 GB RAM (Compute Optimized)
      capacity_type  = "ON_DEMAND"

      labels = {
        role = "application"
      }
    }
  }
}

# ─────────────── 3. Database: Aurora PostgreSQL (Multi-AZ) ───────────────

module "aurora_postgresql" {
  source  = "terraform-aws-modules/rds-aurora/aws"
  version = "9.2.0"

  name           = "payflow-${var.environment}-aurora-pg"
  engine         = "aurora-postgresql"
  engine_version = "16.1"
  instance_class = "db.r6g.xlarge" # Memory optimized for high-volume ACID writes

  instances = {
    primary = {
      instance_class      = "db.r6g.xlarge"
      publicly_accessible = false
    }
    reader_az2 = {
      instance_class      = "db.r6g.xlarge"
      promotion_tier      = 1
      publicly_accessible = false
    }
  }

  vpc_id                 = module.vpc.vpc_id
  db_subnet_group_name   = module.vpc.database_subnet_group_name
  create_db_subnet_group = false

  security_group_rules = {
    vpc_ingress = {
      cidr_blocks = module.vpc.private_subnets_cidr_blocks
    }
  }

  storage_encrypted   = true
  apply_immediately   = false
  monitoring_interval = 10
  auto_minor_version_upgrade = true
  backup_retention_period    = 30
}

# ─────────────── 4. Caching & Distributed Locks: ElastiCache Redis ───────────────

resource "aws_elasticache_replication_group" "redis" {
  replication_group_id          = "payflow-${var.environment}-redis"
  description                   = "PayFlow Redis cluster for idempotency and locks"
  node_type                     = "cache.r6g.large"
  num_cache_clusters            = 3 # Primary + 2 Multi-AZ Replicas with Auto-Failover
  automatic_failover_enabled    = true
  multi_az_enabled              = true
  port                          = 6379
  subnet_group_name             = aws_elasticache_subnet_group.redis.name
  security_group_ids            = [aws_security_group.redis.id]
  at_rest_encryption_enabled    = true
  transit_encryption_enabled   = true
  auth_token                    = var.redis_auth_token
}

resource "aws_elasticache_subnet_group" "redis" {
  name       = "payflow-${var.environment}-redis-subnet-group"
  subnet_ids = module.vpc.private_subnets
}

resource "aws_security_group" "redis" {
  name        = "payflow-${var.environment}-redis-sg"
  description = "Security group for PayFlow Redis cluster"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port   = 6379
    to_port     = 6379
    protocol    = "tcp"
    cidr_blocks = module.vpc.private_subnets_cidr_blocks
  }
}

# ─────────────── 5. Event Streaming: Amazon MSK (Managed Kafka) ───────────────

resource "aws_msk_cluster" "kafka" {
  cluster_name           = "payflow-${var.environment}-msk"
  kafka_version          = "3.6.0"
  number_of_broker_nodes = 3

  broker_node_group_info {
    instance_type   = "kafka.m5.xlarge"
    client_subnets  = module.vpc.private_subnets
    security_groups = [aws_security_group.msk.id]
    storage_info {
      ebs_storage_info {
        volume_size = 500
      }
    }
  }

  encryption_info {
    encryption_in_transit {
      client_broker = "TLS"
      in_cluster    = true
    }
  }
}

resource "aws_security_group" "msk" {
  name        = "payflow-${var.environment}-msk-sg"
  description = "Security group for PayFlow MSK cluster"
  vpc_id      = module.vpc.vpc_id

  ingress {
    from_port   = 9092
    to_port     = 9096
    protocol    = "tcp"
    cidr_blocks = module.vpc.private_subnets_cidr_blocks
  }
}
