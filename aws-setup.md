# AWS Deployment Setup

## Prerequisites
1. **AWS CLI** installed on Jenkins server
2. **AWS credentials** configured in Jenkins
3. **S3 bucket** for storing artifacts
4. **EC2 instance** (optional) for deployment

## Jenkins Setup

### 1. Install AWS CLI Plugin
- Manage Jenkins → Plugins
- Install "AWS CLI Plugin"

### 2. Add AWS Credentials
- Manage Jenkins → Credentials
- Add AWS credentials with ID: `aws-credentials`

### 3. Update Environment Variables
```groovy
environment {
    AWS_REGION = 'your-region'
    S3_BUCKET = 'your-bucket-name'
    EC2_INSTANCE = 'your-instance-id'
}
```

## Deployment Options

### Option 1: S3 Only
- Uploads JARs and frontend to S3
- Manual deployment from S3

### Option 2: S3 + EC2
- Uploads to S3
- Triggers deployment on EC2 via SSM

### Option 3: ECS/EKS
- Build Docker images
- Deploy to container services

## Usage
1. **Local Only**: Run pipeline normally
2. **AWS Deployment**: Check "Deploy to AWS" parameter
3. **Conflicts**: Local runs first, AWS is optional

No conflicts - AWS deployment is conditional!