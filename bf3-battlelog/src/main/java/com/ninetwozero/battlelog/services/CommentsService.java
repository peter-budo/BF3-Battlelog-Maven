package com.ninetwozero.battlelog.services;

import com.ninetwozero.battlelog.datatypes.CommentData;
import com.ninetwozero.battlelog.datatypes.PostData;
import com.ninetwozero.battlelog.datatypes.ProfileData;
import com.ninetwozero.battlelog.datatypes.WebsiteHandlerException;
import com.ninetwozero.battlelog.misc.Constants;
import com.ninetwozero.battlelog.misc.RequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CommentsService {
    public static ArrayList<CommentData> getCommentsForPost(long postId)
            throws WebsiteHandlerException {

        try {

            // Let's do this!
            RequestHandler wh = new RequestHandler();
            ArrayList<CommentData> comments = new ArrayList<CommentData>();
            String httpContent;

            // Get the content
            httpContent = wh.get(

                    Constants.URL_FEED_COMMENTS.replace(

                            "{POST_ID}", postId + ""

                    ), 0

            );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Get the messages
                JSONArray commentArray = new JSONObject(httpContent)
                        .getJSONObject("data").getJSONArray("comments");
                JSONObject tempObject;

                // Iterate
                for (int i = 0, max = commentArray.length(); i < max; i++) {

                    tempObject = commentArray.optJSONObject(i);
                    JSONObject tempOwnerItem = tempObject
                            .getJSONObject("owner");
                    comments.add(

                            new CommentData(

                                    postId,
                                    Long.parseLong(tempObject.getString("itemId")),
                                    Long.parseLong(tempObject.getString("creationDate")),
                                    tempObject.getString("body"), new ProfileData(

                                    tempOwnerItem.getString("username"), null, 0,
                                    Long.parseLong(tempObject
                                            .getString("ownerId")), 0,
                                    tempOwnerItem.getString("gravatarMd5")

                            )

                            )

                    );

                }

                return comments;

            } else {

                throw new WebsiteHandlerException("Could not get the comments.");

            }

        } catch (Exception ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }
    }

    public static boolean commentOnFeedPost(long postId, String checksum,
                                            String comment) throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent;

            // Get the content
            httpContent = wh.post(

                    Constants.URL_COMMENT.replace("{POST_ID}", postId + ""),
                    new PostData[]{

                            new PostData(

                                    Constants.FIELD_NAMES_FEED_COMMENT[0], comment

                            ), new PostData(

                            Constants.FIELD_NAMES_FEED_COMMENT[1], checksum

                    )
                    }, 2 // Noticed the 2?

            );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Hopefully this goes as planned
                if (!httpContent.equals("Internal server error")) {

                    return true;

                } else {

                    return false;

                }

            } else {

                throw new WebsiteHandlerException("Could not post the comment.");

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static boolean postToWall(long profileId, String checksum,
                                     String content, boolean isPlatoon) throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent = wh.post(

                    Constants.URL_FEED_POST, new PostData[]{

                    new PostData(

                            Constants.FIELD_NAMES_FEED_POST[0], content

                    ),
                    new PostData(

                            Constants.FIELD_NAMES_FEED_POST[1], checksum

                    ),
                    new PostData(

                            Constants.FIELD_NAMES_FEED_POST[(!isPlatoon ? 2 : 3)],
                            profileId + ""

                    )
            }, 2

            );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Check the JSON
                String status = new JSONObject(httpContent).optString(
                        "message", "");
                if (status.equals("WALL_POST_CREATED")
                        || status.equals("PLATOONWALL_POST_CREATED")) {

                    return true;

                } else {

                    return false;

                }

            } else {

                return false;

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }
}