output"eks_cluster_name" {
  description = "EKS cluster name"
  value       = module.eks.cluster_name
}

output "eks_cluster_endpoint" {
  description = "EKS API server endpoint"
  value       = module.eks.cluster_endpoint
}

output "aurora_cluster_endpoint" {
  description = "Aurora PostgreSQL write endpoint"
  value       = module.aurora_postgresql.cluster_endpoint
}

output "aurora_cluster_reader_endpoint" {
  description = "Aurora PostgreSQL read-only replica endpoint"
  value       = module.aurora_postgresql.cluster_reader_endpoint
}

output "redis_primary_endpoint" {
  description = "ElastiCache Redis primary endpoint"
  value       = aws_elasticache_replication_group.redis.primary_endpoint_address
}

output "kafka_bootstrap_brokers_tls" {
  description = "Amazon MSK TLS bootstrap broker connection string"
  value       = aws_msk_cluster.kafka.bootstrap_brokers_tls
}
