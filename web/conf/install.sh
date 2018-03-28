#!/bin/bash

# create user
useradd --system -U -M emonlight-xbee-gw -s /bin/false -d /var/lib/emonlight-xbee-gw

# create base dirs
mkdir /var/lib/emonlight-xbee-gw
mkdir /var/log/emonlight-xbee-gw

chown -R emonlight-gw:emonlight-gw /var/log/emonlight-gw

cat > /etc/systemd/system/emonlight-web.service <<EOF
[Unit]
Description=Emonlight Web Application
#After=emonlight-xbee-gw.service
#Requires=emonlight-xbee-gw.service

[Service]
User=emonlight-xbee-gw
#Environment=JAVA_OPTS=-Djava.library.path=/usr/lib/jni
WorkingDirectory=/var/lib/emonlight-xbee-gw
ExecStart=/var/lib/emonlight-xbee-gw/emonlight-web.jar --spring.config.location=/etc/emonlight-web.yml
SuccessExitStatus=143
#Restart=always
#RestartSec=30

[Install]
WantedBy=multi-user.target
EOF
