#!/bin/bash

# remove service
systemctl stop emonlight-web
systemctl disable emonlight-web

rm /etc/systemd/system/emonlight-web.service

# remove dirs if empty
rmdir /var/lib/emonlight-xbee-gw
rmdir /var/log/emonlight-xbee-gw

# remove user
userdel emonlight-xbee-gw
