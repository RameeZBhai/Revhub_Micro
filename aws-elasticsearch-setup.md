# AWS Elasticsearch/OpenSearch Setup for RevHub

## Step 1: Add Elasticsearch Permission to IAM User

### In IAM User Permissions:
- Add policy: `AmazonOpenSearchServiceFullAccess`

## Step 2: Create OpenSearch Domain

### 2.1 Create OpenSearch Cluster
- AWS Console → OpenSearch Service → Create domain
- Domain name: `revhub-search`
- Version: OpenSearch 2.3
- Instance type: t3.small.search (free tier eligible)
- Number of nodes: 1
- Storage: 10 GB EBS
- Network: Public access
- Access policy: Allow open access (for development)

## Step 3: Update Application Configuration

### Add to aws-config.yml:
```yaml
# Elasticsearch Configuration
elasticsearch:
  host: search-revhub-search-xyz.us-east-1.es.amazonaws.com
  port: 443
  protocol: https
  username: ${ES_USERNAME}
  password: ${ES_PASSWORD}
```

## Step 4: Update Search Service

### Add Elasticsearch dependency to search-service pom.xml:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

## Step 5: Update Jenkinsfile Environment

### Add to environment section:
```groovy
OPENSEARCH_ENDPOINT = 'search-revhub-search-xyz.us-east-1.es.amazonaws.com'
```

## Step 6: Update Deployment Script

### Add to deploy-aws.sh:
```bash
# Set Elasticsearch environment variables
export ELASTICSEARCH_HOST=search-revhub-search-xyz.us-east-1.es.amazonaws.com
export ELASTICSEARCH_PORT=443
export ELASTICSEARCH_PROTOCOL=https
```

## Usage
- Search indexing: Automatic via search-service
- Search queries: Through API Gateway port 8087
- Monitoring: AWS OpenSearch dashboard