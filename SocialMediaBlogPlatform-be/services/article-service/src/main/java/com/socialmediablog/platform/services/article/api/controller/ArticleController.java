package com.socialmediablog.platform.services.article.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.article.api.dto.ArticlePageResponse;
import com.socialmediablog.platform.services.article.api.dto.ArticleRequest;
import com.socialmediablog.platform.services.article.api.dto.ArticleResponse;
import com.socialmediablog.platform.services.article.api.dto.CurateArticleRequest;
import com.socialmediablog.platform.services.article.api.dto.RecordArticleViewRequest;
import com.socialmediablog.platform.services.article.api.dto.ServiceStatusResponse;
import com.socialmediablog.platform.services.article.api.dto.UploadArticleMediaResponse;
import com.socialmediablog.platform.services.article.application.command.ArticleActionCommand;
import com.socialmediablog.platform.services.article.application.command.CurateArticleCommand;
import com.socialmediablog.platform.services.article.application.command.CreateArticleCommand;
import com.socialmediablog.platform.services.article.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.article.application.command.ListCuratedArticlesCommand;
import com.socialmediablog.platform.services.article.application.command.ListMyArticlesCommand;
import com.socialmediablog.platform.services.article.application.command.ListPublishedArticlesCommand;
import com.socialmediablog.platform.services.article.application.command.RecordArticleViewCommand;
import com.socialmediablog.platform.services.article.application.command.UpdateArticleCommand;
import com.socialmediablog.platform.services.article.application.command.UploadArticleMediaCommand;
import com.socialmediablog.platform.services.article.application.port.in.ArchiveArticleUseCase;
import com.socialmediablog.platform.services.article.application.port.in.CreateArticleUseCase;
import com.socialmediablog.platform.services.article.application.port.in.CurateArticleUseCase;
import com.socialmediablog.platform.services.article.application.port.in.DeleteArticleUseCase;
import com.socialmediablog.platform.services.article.application.port.in.GetArticleBySlugUseCase;
import com.socialmediablog.platform.services.article.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.article.application.port.in.ListEditorPicksUseCase;
import com.socialmediablog.platform.services.article.application.port.in.ListFeaturedArticlesUseCase;
import com.socialmediablog.platform.services.article.application.port.in.ListMyArticlesUseCase;
import com.socialmediablog.platform.services.article.application.port.in.ListPublishedArticlesUseCase;
import com.socialmediablog.platform.services.article.application.port.in.PublishArticleUseCase;
import com.socialmediablog.platform.services.article.application.port.in.RecordArticleViewUseCase;
import com.socialmediablog.platform.services.article.application.port.in.UpdateArticleUseCase;
import com.socialmediablog.platform.services.article.application.port.in.UploadArticleMediaUseCase;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final GetServiceStatusUseCase getServiceStatusUseCase;
    private final CreateArticleUseCase createArticleUseCase;
    private final UpdateArticleUseCase updateArticleUseCase;
    private final PublishArticleUseCase publishArticleUseCase;
    private final ArchiveArticleUseCase archiveArticleUseCase;
    private final DeleteArticleUseCase deleteArticleUseCase;
    private final GetArticleBySlugUseCase getArticleBySlugUseCase;
    private final ListPublishedArticlesUseCase listPublishedArticlesUseCase;
    private final ListMyArticlesUseCase listMyArticlesUseCase;
    private final ListFeaturedArticlesUseCase listFeaturedArticlesUseCase;
    private final ListEditorPicksUseCase listEditorPicksUseCase;
    private final CurateArticleUseCase curateArticleUseCase;
    private final UploadArticleMediaUseCase uploadArticleMediaUseCase;
    private final RecordArticleViewUseCase recordArticleViewUseCase;

    public ArticleController(
            GetServiceStatusUseCase getServiceStatusUseCase,
            CreateArticleUseCase createArticleUseCase,
            UpdateArticleUseCase updateArticleUseCase,
            PublishArticleUseCase publishArticleUseCase,
            ArchiveArticleUseCase archiveArticleUseCase,
            DeleteArticleUseCase deleteArticleUseCase,
            GetArticleBySlugUseCase getArticleBySlugUseCase,
            ListPublishedArticlesUseCase listPublishedArticlesUseCase,
            ListMyArticlesUseCase listMyArticlesUseCase,
            ListFeaturedArticlesUseCase listFeaturedArticlesUseCase,
            ListEditorPicksUseCase listEditorPicksUseCase,
            CurateArticleUseCase curateArticleUseCase,
            UploadArticleMediaUseCase uploadArticleMediaUseCase,
            RecordArticleViewUseCase recordArticleViewUseCase
    ) {
        this.getServiceStatusUseCase = getServiceStatusUseCase;
        this.createArticleUseCase = createArticleUseCase;
        this.updateArticleUseCase = updateArticleUseCase;
        this.publishArticleUseCase = publishArticleUseCase;
        this.archiveArticleUseCase = archiveArticleUseCase;
        this.deleteArticleUseCase = deleteArticleUseCase;
        this.getArticleBySlugUseCase = getArticleBySlugUseCase;
        this.listPublishedArticlesUseCase = listPublishedArticlesUseCase;
        this.listMyArticlesUseCase = listMyArticlesUseCase;
        this.listFeaturedArticlesUseCase = listFeaturedArticlesUseCase;
        this.listEditorPicksUseCase = listEditorPicksUseCase;
        this.curateArticleUseCase = curateArticleUseCase;
        this.uploadArticleMediaUseCase = uploadArticleMediaUseCase;
        this.recordArticleViewUseCase = recordArticleViewUseCase;
    }

    @GetMapping("/status")
    public ApiResponse<ServiceStatusResponse> status(@AuthenticationPrincipal CurrentUser currentUser) {
        String currentUserId = currentUser == null ? "anonymous" : currentUser.id();
        return ApiResponse.success(ServiceStatusResponse.from(getServiceStatusUseCase.execute(
                new GetServiceStatusCommand(currentUserId)
        )));
    }

    @GetMapping
    public ApiResponse<ArticlePageResponse> listPublished(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) String tag,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(ArticlePageResponse.from(listPublishedArticlesUseCase.execute(
                new ListPublishedArticlesCommand(category, authorId, tag, query, sort, page, size)
        )));
    }

    @GetMapping("/featured")
    public ApiResponse<List<ArticleResponse>> featured(@RequestParam(defaultValue = "1") int size) {
        return ApiResponse.success(listFeaturedArticlesUseCase.executeFeatured(new ListCuratedArticlesCommand(size)).stream()
                .map(ArticleResponse::from)
                .toList());
    }

    @GetMapping("/editor-picks")
    public ApiResponse<List<ArticleResponse>> editorPicks(@RequestParam(defaultValue = "2") int size) {
        return ApiResponse.success(listEditorPicksUseCase.executeEditorPicks(new ListCuratedArticlesCommand(size)).stream()
                .map(ArticleResponse::from)
                .toList());
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<ArticleResponse> detail(@PathVariable String slug) {
        return ApiResponse.success(ArticleResponse.from(getArticleBySlugUseCase.executeBySlug(slug)));
    }

    @GetMapping("/me")
    public ApiResponse<ArticlePageResponse> myArticles(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(ArticlePageResponse.from(listMyArticlesUseCase.execute(
                new ListMyArticlesCommand(currentUserId(currentUser), status, page, size)
        )));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ArticleResponse> create(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody ArticleRequest request
    ) {
        return ApiResponse.success("Article draft created", ArticleResponse.from(createArticleUseCase.execute(
                new CreateArticleCommand(
                        currentUserId(currentUser),
                        request.title(),
                        request.slug(),
                        request.category(),
                        request.summary(),
                        request.content(),
                        request.coverImageUrl(),
                        request.tags()
                )
        )));
    }

    @PutMapping("/{articleId}")
    public ApiResponse<ArticleResponse> update(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId,
            @Valid @RequestBody ArticleRequest request
    ) {
        return ApiResponse.success("Article updated", ArticleResponse.from(updateArticleUseCase.execute(
                new UpdateArticleCommand(
                        articleId,
                        currentUserId(currentUser),
                        request.title(),
                        request.slug(),
                        request.category(),
                        request.summary(),
                        request.content(),
                        request.coverImageUrl(),
                        request.tags()
                )
        )));
    }

    @PostMapping("/{articleId}/publish")
    public ApiResponse<ArticleResponse> publish(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId
    ) {
        return ApiResponse.success("Article published", ArticleResponse.from(publishArticleUseCase.publish(
                new ArticleActionCommand(articleId, currentUserId(currentUser))
        )));
    }

    @PostMapping("/{articleId}/archive")
    public ApiResponse<ArticleResponse> archive(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId
    ) {
        return ApiResponse.success("Article archived", ArticleResponse.from(archiveArticleUseCase.archive(
                new ArticleActionCommand(articleId, currentUserId(currentUser))
        )));
    }

    @DeleteMapping("/{articleId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId
    ) {
        deleteArticleUseCase.delete(new ArticleActionCommand(articleId, currentUserId(currentUser)));
        return ApiResponse.success("Article deleted", null);
    }

    @PatchMapping("/{articleId}/curation")
    public ApiResponse<ArticleResponse> curate(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId,
            @RequestBody CurateArticleRequest request
    ) {
        ensureAdmin(currentUser);
        return ApiResponse.success("Article curation updated", ArticleResponse.from(curateArticleUseCase.curate(
                new CurateArticleCommand(articleId, request.featuredRank(), request.editorPickRank())
        )));
    }

    @PostMapping("/media")
    public ApiResponse<UploadArticleMediaResponse> uploadMedia(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ApiResponse.success("Article media uploaded", UploadArticleMediaResponse.from(uploadArticleMediaUseCase.execute(
                new UploadArticleMediaCommand(
                        currentUserId(currentUser),
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getBytes()
                )
        )));
    }

    @PostMapping("/{articleId}/views")
    public ApiResponse<Void> recordView(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID articleId,
            @Valid @RequestBody(required = false) RecordArticleViewRequest request
    ) {
        recordArticleViewUseCase.execute(new RecordArticleViewCommand(
                articleId,
                currentUser == null ? null : UUID.fromString(currentUser.id()),
                request == null ? null : request.anonymousViewerKey(),
                request == null ? null : request.source()
        ));
        return ApiResponse.success("Article view recorded", null);
    }

    private UUID currentUserId(CurrentUser currentUser) {
        return UUID.fromString(currentUser.id());
    }

    private void ensureAdmin(CurrentUser currentUser) {
        if (currentUser == null || !currentUser.hasRole("ADMIN")) {
            throw new com.socialmediablog.platform.services.article.application.exception.ForbiddenArticleActionException(
                    "Only admins can curate articles"
            );
        }
    }
}
