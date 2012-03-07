package com.ninetwozero.battlelog.services;

import android.content.Context;
import com.ninetwozero.battlelog.datatypes.Board;
import com.ninetwozero.battlelog.datatypes.PostData;
import com.ninetwozero.battlelog.datatypes.ProfileData;
import com.ninetwozero.battlelog.datatypes.WebsiteHandlerException;
import com.ninetwozero.battlelog.misc.Constants;
import com.ninetwozero.battlelog.misc.RequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ForumService {
    public static Object[] getAllForums(String locale)
            throws WebsiteHandlerException {

        try {

            // Init to win it
            ArrayList<Board.Forum> forums = new ArrayList<Board.Forum>();
            String title = "";

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.get(

                    Constants.URL_FORUM_LIST_LOCALIZED.replace("{LOCALE}", locale), 1

            );

            // Let's parse it!
            JSONArray categoryArray = new JSONObject(httpContent)
                    .getJSONObject("context").getJSONArray("categories");
            JSONArray forumArray = categoryArray.getJSONObject(0).optJSONArray(
                    "forums");

            // Get the title
            title = categoryArray.getJSONObject(0).getString("title");

            // Loop
            for (int i = 0, max = forumArray.length(); i < max; i++) {

                // Get the current object
                JSONObject currObject = forumArray.getJSONObject(i);
                JSONObject lastThread = currObject.optJSONObject("lastThread");

                // Let's store them
                if (lastThread != null) {

                    JSONObject userInfo = lastThread
                            .optJSONObject("lastPoster");

                    if (userInfo != null) {

                        forums.add(

                                new Board.Forum(

                                        Long.parseLong(currObject.getString("id")), Long
                                        .parseLong(currObject.getString("categoryId")),
                                        lastThread.getLong("lastPostDate"), Long
                                        .parseLong(lastThread.getString("id")),
                                        Long.parseLong(lastThread
                                                .getString("lastPostId")), currObject
                                        .getLong("numberOfPosts"), currObject
                                        .getLong("numberOfThreads"), 0,
                                        currObject.getString("title"), currObject
                                        .getString("description"), lastThread
                                        .getString("title"), userInfo
                                        .getString("username")

                                )

                        );

                    }

                } else {

                    forums.add(

                            new Board.Forum(

                                    Long.parseLong(currObject.getString("id")), Long
                                    .parseLong(currObject.getString("categoryId")), 0,
                                    0, 0, currObject.getLong("numberOfPosts"),
                                    currObject.getLong("numberOfThreads"), 0,
                                    currObject.getString("title"), currObject
                                    .getString("description"), null, null

                            )

                    );

                }

            }

            // Return
            return new Object[]{
                    title, forums
            };

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("No forums found.");

        }

    }

    public static Board.Forum getThreadsForForum(String locale, long forumId)
            throws WebsiteHandlerException {

        try {

            // Init to winit
            ArrayList<Board.ThreadData> threads = new ArrayList<Board.ThreadData>();

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.get(

                    Constants.URL_FORUM_FORUM.replace("{LOCALE}", locale)
                            .replace("{FORUM_ID}", forumId + "")
                            .replace("{PAGE}", 1 + ""), 1

            );

            // Let's parse it!
            JSONObject contextObject = new JSONObject(httpContent)
                    .getJSONObject("context");
            JSONObject forumObject = contextObject.getJSONObject("forum");
            JSONArray stickiesArray = contextObject.getJSONArray("stickies");
            JSONArray threadArray = contextObject.getJSONArray("threads");

            // Loop the stickies
            int numStickies = stickiesArray.length();
            for (int i = 0, max = numStickies; i < max; i++) {

                // Yay, we found at least one sticky
                if (i == 0) {
                    threads.add(new Board.ThreadData("Stickies"));
                }

                // Get the current object
                JSONObject currObject = stickiesArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject
                        .getJSONObject("lastPoster");

                // Let's store them
                threads.add(

                        new Board.ThreadData(

                                Long.parseLong(currObject.getString("id")), currObject
                                .getLong("creationDate"), currObject
                                .getLong("lastPostDate"), currObject
                                .getInt("numberOfOfficialPosts"), currObject
                                .getInt("numberOfPosts"),
                                currObject.getString("title"), new ProfileData(

                                ownerObject.getString("username"), ownerObject
                                .getString("username"), Long
                                .parseLong(ownerObject.getString("userId")), 0,
                                0, ownerObject.getString("gravatarMd5")

                        ), new ProfileData(

                                lastPosterObject.getString("username"),
                                lastPosterObject.getString("username"), Long
                                .parseLong(lastPosterObject
                                        .getString("userId")), 0, 0,
                                lastPosterObject.getString("gravatarMd5")

                        ), currObject.getBoolean("isSticky"), currObject
                                .getBoolean("isLocked")

                        )

                );

            }

            // Loop the regular
            for (int i = numStickies, max = threadArray.length(); i < max; i++) {

                if (i == numStickies) {
                    threads.add(new Board.ThreadData("Threads"));
                }

                // Get the current object
                JSONObject currObject = threadArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject
                        .getJSONObject("lastPoster");

                // Let's store them
                threads.add(

                        new Board.ThreadData(

                                Long.parseLong(currObject.getString("id")), currObject
                                .getLong("creationDate"), currObject
                                .getLong("lastPostDate"), currObject
                                .getInt("numberOfOfficialPosts"), currObject
                                .getInt("numberOfPosts"),
                                currObject.getString("title"), new ProfileData(

                                ownerObject.getString("username"), ownerObject
                                .getString("username"), Long
                                .parseLong(ownerObject.getString("userId")), 0,
                                0, ownerObject.getString("gravatarMd5")

                        ), new ProfileData(

                                lastPosterObject.getString("username"),
                                lastPosterObject.getString("username"), Long
                                .parseLong(lastPosterObject
                                        .getString("userId")), 0, 0,
                                lastPosterObject.getString("gravatarMd5")

                        ), currObject.getBoolean("isSticky"), currObject
                                .getBoolean("isLocked")

                        )

                );

            }

            return new Board.Forum(

                    forumObject.getString("title"),
                    forumObject.getString("description"),
                    forumObject.getLong("numberOfPosts"),
                    forumObject.getLong("numberOfThreads"),
                    contextObject.getLong("numPages"), threads

            );

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("No threads found.");

        }

    }

    public static Board.ThreadData getPostsForThread(String locale,
                                                     long threadId) throws WebsiteHandlerException {

        try {

            // Init to winit
            ArrayList<Board.PostData> posts = new ArrayList<Board.PostData>();

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.get(

                    Constants.URL_FORUM_THREAD.replace("{LOCALE}", locale)
                            .replace("{THREAD_ID}", threadId + "")
                            .replace("{PAGE}", "1"), 1

            );

            // Let's parse it!
            JSONObject contextObject = new JSONObject(httpContent)
                    .getJSONObject("context");
            JSONArray postArray = contextObject.getJSONArray("posts");
            JSONObject threadObject = contextObject.getJSONObject("thread");
            JSONObject lastPosterObject = threadObject
                    .getJSONObject("lastPoster");
            JSONObject threadOwnerObject = threadObject.getJSONObject("owner");

            // Loop the stickies
            for (int i = 0, max = postArray.length(); i < max; i++) {

                // Get the current object
                JSONObject currObject = postArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");

                // Let's store them
                posts.add(

                        new Board.PostData(

                                Long.parseLong(currObject.getString("id")), Long
                                .parseLong(currObject.getString("creationDate")), Long
                                .parseLong(currObject.getString("threadId")),
                                new ProfileData(

                                        ownerObject.getString("username"), ownerObject
                                        .getString("username"), 0, Long
                                        .parseLong(ownerObject.getString("userId")), 0,
                                        ownerObject.optString("gravatarMd5", "")

                                ), currObject.getString("formattedBody"), currObject
                                .getInt("abuseCount"), currObject
                                .getBoolean("isCensored"), currObject
                                .getBoolean("isOfficial")

                        )

                );

            }

            return new Board.ThreadData(

                    threadObject.getLong("id"), threadObject.getLong("creationDate"),
                    threadObject.getLong("lastPostDate"),
                    threadObject.getInt("numberOfOfficialPosts"),
                    threadObject.getInt("numberOfPosts"),
                    contextObject.getInt("currentPage"),
                    contextObject.getInt("numPages"),
                    threadObject.getString("title"), new ProfileData(

                    threadOwnerObject.getString("username"),
                    threadOwnerObject.getString("username"),
                    Long.parseLong(threadOwnerObject
                            .getString("userId")), 0, 0,
                    threadOwnerObject.getString("gravatarMd5")

            ),
                    new ProfileData(

                            lastPosterObject.getString("username"), lastPosterObject
                            .getString("username"), Long
                            .parseLong(lastPosterObject.getString("userId")),
                            0, 0, lastPosterObject.getString("gravatarMd5")

                    ), threadObject.getBoolean("isSticky"),
                    threadObject.getBoolean("isLocked"),
                    contextObject.getBoolean("canEditPosts"),
                    contextObject.getBoolean("canCensorPosts"),
                    contextObject.getBoolean("canDeletePosts"),
                    contextObject.getBoolean("canPostOfficial"),
                    contextObject.getBoolean("canViewLatestPosts"),
                    contextObject.getBoolean("canViewPostHistory"),
                    contextObject.getBoolean("isAdmin"), posts

            );

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("No threads found.");

        }

    }

    public static boolean postReplyInThread(final Context c, final String body,
                                            final String chksm, final long tId) {

        try {

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();

            // POST!
            String httpContent = rh.post(

                    Constants.URL_FORUM_POST.replace("{THREAD_ID}", tId + ""),
                    new PostData[]{

                            new PostData(Constants.FIELD_NAMES_FORUM_POST[0],
                                    body),
                            new PostData(Constants.FIELD_NAMES_FORUM_POST[1],
                                    chksm)

                    }, 1

            );

            // Let's do it
            return (!"".equals(httpContent));

        } catch (Exception ex) {

            ex.printStackTrace();
            return false;

        }

    }

    public static boolean createNewThreadInForum(final Context c,
                                                 final String topic, final String body, final String chksm,
                                                 final long fId) {

        try {

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();

            // POST!
            String httpContent = rh.post(

                    Constants.URL_FORUM_NEW.replace("{FORUM_ID}", fId + ""),
                    new PostData[]{

                            new PostData(Constants.FIELD_NAMES_FORUM_NEW[0],
                                    topic),
                            new PostData(Constants.FIELD_NAMES_FORUM_NEW[1],
                                    body),
                            new PostData(Constants.FIELD_NAMES_FORUM_NEW[2],
                                    chksm)

                    }, 1

            );

            // Let's do it
            return (!"".equals(httpContent));

        } catch (Exception ex) {

            ex.printStackTrace();
            return false;

        }

    }

    public static ArrayList<Board.SearchResult> searchInForums(

            final Context c, final String keyword

    ) throws WebsiteHandlerException {

        // Init
        RequestHandler rh = new RequestHandler();
        ArrayList<Board.SearchResult> threads = new ArrayList<Board.SearchResult>();

        try {

            // Let's do the actual search
            String httpContent = rh.get(

                    Constants.URL_FORUM_SEARCH.replace("{SEARCH_STRING}", keyword), 1

            );
            if (!"".equals(httpContent)) {

                // Let's parse it as JSON
                JSONArray threadArray = new JSONObject(httpContent)
                        .getJSONObject("data").getJSONArray("results");
                int numResults = threadArray.length();

                // Let's see...
                if (numResults > 0) {

                    // Iterate over the results!
                    for (int i = 0; i < numResults; i++) {

                        // Let's get the current result-set
                        JSONObject currentItem = threadArray.getJSONObject(i);

                        threads.add(

                                new Board.SearchResult(

                                        Long.parseLong(currentItem.getString("docid")
                                                .substring(2)),
                                        currentItem
                                                .getLong("timestamp"),
                                        currentItem
                                                .getString("title"),
                                        new ProfileData(

                                                currentItem.getString("ownerUsername"),
                                                currentItem
                                                        .getString("ownerUsername"),
                                                Long
                                                        .parseLong(currentItem.getString("ownerId")),
                                                0, 0, null),
                                        currentItem.getBoolean("isSticky"), currentItem
                                        .getBoolean("isOfficial")

                                )

                        );

                    }

                }
            }

            return threads;

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static ArrayList<Board.ThreadData> getThreadsForForum(long forumId,
                                                                 int page, String locale) throws WebsiteHandlerException {

        try {

            // Init to winit
            ArrayList<Board.ThreadData> threads = new ArrayList<Board.ThreadData>();

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.get(

                    Constants.URL_FORUM_FORUM.replace("{LOCALE}", locale)
                            .replace("{FORUM_ID}", forumId + "")
                            .replace("{PAGE}", page + ""), 1

            );

            // Let's parse it!
            JSONObject contextObject = new JSONObject(httpContent)
                    .getJSONObject("context");
            JSONArray stickiesArray = contextObject.getJSONArray("stickies");
            JSONArray threadArray = contextObject.getJSONArray("threads");

            // Loop the stickies
            int numStickies = stickiesArray.length();
            for (int i = 0, max = numStickies; i < max; i++) {

                // Yay, we found at least one sticky
                if (i == 0 && page == 1) {
                    threads.add(new Board.ThreadData("Stickies"));
                }

                // Get the current object
                JSONObject currObject = stickiesArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject
                        .getJSONObject("lastPoster");

                // Let's store them
                threads.add(

                        new Board.ThreadData(

                                Long.parseLong(currObject.getString("id")), currObject
                                .getLong("creationDate"), currObject
                                .getLong("lastPostDate"), currObject
                                .getInt("numberOfOfficialPosts"), currObject
                                .getInt("numberOfPosts"),
                                currObject.getString("title"), new ProfileData(

                                ownerObject.getString("username"), ownerObject
                                .getString("username"), Long
                                .parseLong(ownerObject.getString("userId")), 0,
                                0, ownerObject.getString("gravatarMd5")

                        ), new ProfileData(

                                lastPosterObject.getString("username"),
                                lastPosterObject.getString("username"), Long
                                .parseLong(lastPosterObject
                                        .getString("userId")), 0, 0,
                                lastPosterObject.getString("gravatarMd5")

                        ), currObject.getBoolean("isSticky"), currObject
                                .getBoolean("isLocked")

                        )

                );

            }

            // Loop the regular
            for (int i = numStickies, max = threadArray.length(); i < max; i++) {

                if (i == numStickies && page == 1) {
                    threads.add(new Board.ThreadData("Threads"));
                }

                // Get the current object
                JSONObject currObject = threadArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");
                JSONObject lastPosterObject = currObject
                        .getJSONObject("lastPoster");

                // Let's store them
                threads.add(

                        new Board.ThreadData(

                                Long.parseLong(currObject.getString("id")), currObject
                                .getLong("creationDate"), currObject
                                .getLong("lastPostDate"), currObject
                                .getInt("numberOfOfficialPosts"), currObject
                                .getInt("numberOfPosts"),
                                currObject.getString("title"), new ProfileData(

                                ownerObject.getString("username"), ownerObject
                                .getString("username"), Long
                                .parseLong(ownerObject.getString("userId")), 0,
                                0, ownerObject.getString("gravatarMd5")

                        ), new ProfileData(

                                lastPosterObject.getString("username"),
                                lastPosterObject.getString("username"), Long
                                .parseLong(lastPosterObject
                                        .getString("userId")), 0, 0,
                                lastPosterObject.getString("gravatarMd5")

                        ), currObject.getBoolean("isSticky"), currObject
                                .getBoolean("isLocked")

                        )

                );

            }

            return threads;

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("No threads found.");

        }

    }

    public static ArrayList<Board.PostData> getPostsForThread(long threadId,
                                                              int page, String locale) throws WebsiteHandlerException {

        try {

            // Init to winit
            ArrayList<Board.PostData> posts = new ArrayList<Board.PostData>();

            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.get(

                    Constants.URL_FORUM_THREAD.replace("{LOCALE}", locale)
                            .replace("{THREAD_ID}", threadId + "")
                            .replace("{PAGE}", page + ""), 1

            );

            // Let's parse it!
            JSONArray postArray = new JSONObject(httpContent).getJSONObject(
                    "context").getJSONArray("posts");

            // Loop the stickies
            for (int i = 0, max = postArray.length(); i < max; i++) {

                // Get the current object
                JSONObject currObject = postArray.getJSONObject(i);
                JSONObject ownerObject = currObject.getJSONObject("owner");

                // Let's store them
                posts.add(

                        new Board.PostData(

                                Long.parseLong(currObject.getString("id")), Long
                                .parseLong(currObject.getString("creationDate")), Long
                                .parseLong(currObject.getString("threadId")),
                                new ProfileData(

                                        ownerObject.getString("username"), ownerObject
                                        .getString("username"), 0, Long
                                        .parseLong(ownerObject.getString("userId")), 0,
                                        ownerObject.optString("gravatarMd5", "")

                                ), currObject.getString("formattedBody"), currObject
                                .getInt("abuseCount"), currObject
                                .getBoolean("isCensored"), currObject
                                .getBoolean("isOfficial")

                        )

                );

            }

            return posts;

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("No threads found.");

        }

    }

    public static boolean reportPostInThread(Context c, long pId, String r)
            throws WebsiteHandlerException {

        try {
            // Setup a RequestHandler
            RequestHandler rh = new RequestHandler();
            final String httpContent = rh.post(

                    Constants.URL_FORUM_REPORT.replace("{POST_ID}", pId + ""),
                    new PostData[]{

                            new PostData(

                                    Constants.FIELD_NAMES_FORUM_REPORT[0], r

                            ), new PostData(

                            Constants.FIELD_NAMES_FORUM_REPORT[1], ""

                    )

                    }, 1

            );

            // Let's parse it!
            int status = new JSONObject(httpContent).getJSONObject("data")
                    .getInt("success");
            return (status == 1);

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("Could not report the post.");

        }

    }
}