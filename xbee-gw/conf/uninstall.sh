#!/bin/bash

# remove service
systemctl stop emonlight-xbee-gw
systemctl disable emonlight-xbee-gw

rm /etc/systemd/system/emonlight-xbee-gw.service

# remove dirs if empty
rmdir /var/lib/emonlight-xbee.gw
rmdir /var/log/emonlight-xbee-gw

# remove user
userdel emonlight-xbee-gw
