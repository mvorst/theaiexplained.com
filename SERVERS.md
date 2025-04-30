# Webserver Setup

## Install Java

sudo yum install java-17-amazon-corretto -y

## Configure AWS CLI

aws --version

aws configure

> AWS Access Key ID [None]: ***********
> 
> AWS Secret Access Key [None]: ************
>
> Default region name [None]: us-west-1
>
> Default output format [None]: json



## Install HTTPD

sudo yum install httpd -y

sudo systemctl start httpd

Create AWS Credentials


### Configure HTTPD
vi /etc/httpd/conf/httpd.conf

sudo systemctl enable httpd

sudo firewall-cmd --permanent --add-port=80/tcp

## Create Server Scripts
sudo mkdir -p /opt/app/

Copy the /src/main/scripts directory to the server (/opt/app/)

sudo chmod +x /opt/app/*.sh

## Create Systemd Service

sudo vi /etc/systemd/system/webapp.service

sudo systemctl daemon-reload

sudo systemctl enable webapp.service

sudo systemctl start webapp.service

sudo systemctl status webapp.service
