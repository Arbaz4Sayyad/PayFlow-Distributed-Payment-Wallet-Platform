variable "aws_region" {
  description = "AWS deployment region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Deployment environment name"
  type        = string
  default     = "production"
}

variable "redis_auth_token" {
  description = "Auth token for Redis ElastiCache cluster"
  type        = string
  sensitive   = true
  default     = "redis_payflow_secure_2026"
}
