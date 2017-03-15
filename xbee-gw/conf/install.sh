#!/bin/bash

# create user
useradd --system -U -M emonlight-xbee-gw -s /bin/false -d /var/lib/emonlight-xbee-gw

# create base dirs
mkdir /var/lib/emonlight-xbee-gw
mkdir /var/log/emonlight-xbee-gw

chown -R emonlight-gw:emonlight-gw /var/log/emonlight-gw

cat > /etc/systemd/system/emonlight-xbee-gw.service <<EOF
[Unit]
Description=Emonlight Xbee Gateway
After=syslog.target

[Service]
User=emonlight
ExecStart=/var/lib/emonlight-xbee-gw/emonlight-xbee-gw.jar -Djava.library.path=/usr/lib/jni --spring-config-name=/etc/emonlight-xbee-gw.yml
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF
