package io.stormbird.token.web;

import org.springframework.web.servlet.NoHandlerFoundException;
import org.xml.sax.SAXException;

import java.io.IOException;

public class DMZUniversalLinkHandler {

    public Response Render(Request req) {
        AppSiteController appSiteController = new AppSiteController();
        //TODO deal with rendering on AWS US
        String res = "";
        try {
            res = appSiteController.handleUniversalLink(req.universalLink, "", null, null);
        } catch (Exception e) {
            //TODO find some way to handle this in lambda
            e.printStackTrace();
        }
        return new Response(res);
    }

    public static class Request {
        String universalLink;

        public String getUniversalLink() {
            return universalLink;
        }

        public void setUniversalLink(String universalLink) {
            this.universalLink = universalLink;
        }

        public Request(String universalLink) {
            this.universalLink = universalLink;
        }

        public Request() {
        }
    }

    public static class Response {
        String page;

        public String getPage() { return page; }

        public void setPage(String page) { this.page = page; }

        public Response(String page) {
            this.page = page;
        }

        public Response() {
        }
    }

}
