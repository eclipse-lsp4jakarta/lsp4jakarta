package io.openliberty.sample.jakarta.pojos;

import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PathParam;

public class ArticleBean {
    @PathParam("authorId")
    private String authorId;

    @PathParam("articleId")
    private String articleId;

    @QueryParam("highlight")
    private boolean highlight;

    @QueryParam("version")
    private int version;

    public String getAuthorId() { return authorId; }
    public String getArticleId() { return articleId; }
    public boolean isHighlight() { return highlight; }
    public int getVersion() { return version; }
}
