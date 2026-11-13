# Social Features: Review Guide

Branch: `codex/release-social-features`, based on `dev`.
These changes are local for review. No commit, push, merge, deployment or release is performed automatically.

## Start a local review

From PowerShell at the repository root:

```powershell
.\scripts\start-review.ps1
```

Open <http://localhost:8081/swagger-ui/index.html>.
Use `-Port 8082` if 8081 is occupied. Stop with Ctrl+C in the server terminal.

This command uses the test runtime with a temporary H2 database, generates a temporary
JWT key, ignores `.env`, binds to localhost and supplies dummy AWS credentials.
Accounts and posts disappear after stopping the server. Nothing is written to Neon.
Email delivery, S3 uploads and Google sign-in are not functional in this isolated preview.
This launcher is for local review, not production.

## API changes

All endpoints below require `Authorization: Bearer <accessToken>`.

| Endpoint | Behavior |
| --- | --- |
| `GET /users/search?q=eco&page=0&size=20` | Partial, case-insensitive name or username search; active users only. |
| `GET /users/search` | Discover active users without a search term. |
| `GET /posts/feed?page=0&size=20` | Posts from active authors followed by the authenticated user. |
| `GET /users/me` | Now includes `emailVerified`. |
| `GET /posts`, `GET /posts/user/{userId}`, `GET /posts/{id}` | Now include interaction summaries, as does the feed. |

Search returns `UserSummaryDTO`: `id`, `username`, `name`, `profileImageUrl`.
It never returns email, verification status, password, role or provider information.
The existing public-profile endpoint and private-user authorization remain available.

Search trims `q`, accepts up to 100 characters and treats `%` and `_` literally.
Empty or whitespace-only terms discover active users. No matches return an empty page.
The default search ordering is `username ASC, id ASC`.

The feed excludes own posts, unfollowed authors and inactive authors.
No follows or no matching posts return HTTP 200 with an empty page.
Follow/unfollow changes are reflected on the next feed request.
The default post ordering is `createdAt DESC, id DESC`.

Pagination retains the existing `content`, `totalElements`, `totalPages`, `number`,
and `size` response structure. Page numbering starts at 0. Default size is 20;
the maximum size is 100. Existing sorting via `sort=field,direction` remains supported.

Every post response adds:

```json
{
  "likeCount": 2,
  "commentCount": 3,
  "likedByCurrentUser": true
}
```

The counts are calculated in batch, without joining likes and comments together.
This avoids both per-post queries and multiplied counts. Unliked posts report `false`;
posts with no interactions report zero counts. Empty pages skip interaction queries.

`emailVerified` is included in the private `UserResponseDTO`, including `/users/me`.
It is excluded from search and public profiles. Registration still succeeds without
email verification, and delivery is requested explicitly through `/auth/resend-verification`.

## Manual walkthrough in Swagger

1. Register account Alice with `POST /auth/register`. Save its returned ID and token.
2. Register account Bob. Authorize with Bob's `accessToken` using the Authorize button
   (paste the token alone, without the `Bearer` prefix).
3. Create two posts as Bob with `POST /posts`; an image is optional.
4. Authorize as Alice and search for Bob by username and by part of his name.
5. Open Bob's public profile through `GET /users/{id}/profile`.
6. Request `/posts/feed`: it should be empty before following Bob.
7. Follow Bob through `POST /users/{id}/follow`, using Bob's ID.
8. Request the feed again: Bob's posts should appear, newest first. Test `size=1&page=0`
   and `size=1&page=1`.
9. Like and comment on a Bob post. Fetch the feed and the individual post again;
   compare their counts and `likedByCurrentUser` values.
10. Log in as Bob: Alice's like remains counted, but Bob's `likedByCurrentUser` is false.
11. Unfollow Bob as Alice and confirm the feed becomes empty.
12. Request `/users/me` as Alice: `emailVerified` should be false. Public profiles and
    search results must not expose that field or Alice's email.

## Swagger changes

- Authentication, users, profiles, search, feed, posts, comments, likes and follows are visible.
- Bearer authorization is shown for protected operations; login and registration are public.
- Pageable parameters appear individually as `page`, `size`, and `sort`.
- Multipart operations declare their content type for file upload forms.
- Interactive requests are enabled in production; existing API authorization still applies.
- Tokens are not persisted by Swagger across reloads.

## Database and validation

`V10__add_social_query_indexes.sql` adds indexes for post ordering, author filtering,
like counts and comment counts. Existing migrations and table data are unchanged.
Flyway applies V10 at the next normal startup with migrations enabled.

Run the build with:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress clean verify
```

The automated tests cover filtering, pagination, feed isolation and ordering,
follow/unfollow, empty results, private-field exclusion, email verification status,
OpenAPI paths/schemas and batch interaction counts (three queries per nonempty batch).
Repository tests use H2. Before production, validate the migration and startup against
PostgreSQL, run the real AWS/Google integration checks, and check the Render health endpoint.

Suggested PR title: `feat(social): add user discovery, following feed and post summaries`
