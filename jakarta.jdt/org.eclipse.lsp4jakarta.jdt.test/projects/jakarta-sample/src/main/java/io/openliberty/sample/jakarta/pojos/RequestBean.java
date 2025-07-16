package io.openliberty.sample.jakarta.pojos;

import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PathParam;

public class RequestBean {
    @PathParam("userId")
    private String userId;

    @PathParam("postId")
    private int postId;

    @QueryParam("sort")
    private String sort;

    @QueryParam("page")
    private int page;

    public String getUserId() { return userId; }
    public int getPostId() { return postId; }
    public String getSort() { return sort; }
    public int getPage() { return page; }
}

