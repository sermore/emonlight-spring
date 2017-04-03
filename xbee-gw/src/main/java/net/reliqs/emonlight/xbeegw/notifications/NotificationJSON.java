package net.reliqs.emonlight.xbeegw.notifications;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * Created by sergio on 01/04/17.
 */
public class NotificationJSON {

    private Notification notification;
    private String to;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public static NotificationJSON create(String title, String body, String to) {
        Notification n = new Notification();
        n.setTitle(title);
        n.setBody(body);
        n.setIcon("/images/profile_placeholder.png");
        n.setClick_action("http://localhost:5000");
        NotificationJSON msg = new NotificationJSON();
        msg.setNotification(n);
        msg.setTo(to);
        return msg;
    }

    static class Notification {

        private String title;
        private String body;
        private String icon;
        private String click_action;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getClick_action() {
            return click_action;
        }

        public void setClick_action(String click_action) {
            this.click_action = click_action;
        }

    }

}
