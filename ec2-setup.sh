#!/bin/bash

# SpendSmart EC2 Setup Script
# Run this once on your fresh EC2 instance

# 1. Update system
sudo yum update -y

# 2. Install Docker
sudo amazon-linux-extras install docker -y
sudo service docker start
sudo usermod -a -G docker ec2-user

# 3. Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# 4. Create project directory
mkdir -p ~/spendsmart
cd ~/spendsmart

# 5. Instructions for the user
echo "=========================================================="
echo " EC2 Setup Complete! "
echo "=========================================================="
echo "Next steps:"
echo "1. Log out and log back in for docker group changes to take effect."
echo "2. Create a .env file in ~/spendsmart/ using .env.example as a template."
echo "3. Add the following GitHub Secrets to your repositories:"
echo "   - DOCKERHUB_USERNAME"
echo "   - DOCKERHUB_TOKEN"
echo "   - EC2_HOST (your EC2 public IP)"
echo "   - EC2_SSH_KEY (your .pem file content)"
echo "   - EC2_USERNAME (usually 'ec2-user' for Amazon Linux)"
echo "=========================================================="
