package com.ninetwozero.battlelog.services;

import android.content.Context;
import com.ninetwozero.battlelog.R;
import com.ninetwozero.battlelog.datatypes.*;
import com.ninetwozero.battlelog.misc.Constants;
import com.ninetwozero.battlelog.misc.RequestHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class FriendsListService {
    public static final FriendListDataWrapper getFriendsCOM(final Context c,
                                                            final String checksum) throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent;

            // Get the content
            httpContent = wh.post(

                    Constants.URL_FRIENDS, new PostData[]{

                    new PostData(

                            Constants.FIELD_NAMES_CHECKSUM[0], checksum

                    )

            }, 0

            );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Generate an object
                JSONObject comData = new JSONObject(httpContent)
                        .getJSONObject("data");
                JSONArray friendsObject = comData
                        .getJSONArray("friendscomcenter");
                JSONArray requestsObject = comData
                        .getJSONArray("friendrequests");
                JSONObject tempObj, presenceObj;

                // Arraylists!
                ArrayList<ProfileData> profileRowRequests = new ArrayList<ProfileData>();
                ArrayList<ProfileData> profileRowPlaying = new ArrayList<ProfileData>();
                ArrayList<ProfileData> profileRowOnline = new ArrayList<ProfileData>();
                ArrayList<ProfileData> profileRowOffline = new ArrayList<ProfileData>();

                // Grab the lengths
                int numRequests = requestsObject.length(), numFriends = friendsObject
                        .length();

                // Got requests?
                if (numRequests > 0) {

                    // Iterate baby!
                    for (int i = 0, max = requestsObject.length(); i < max; i++) {

                        // Grab the object
                        tempObj = requestsObject.optJSONObject(i);

                        // Save it
                        profileRowRequests.add(

                                new ProfileData(

                                        tempObj.getString("username"), tempObj
                                        .getString("username"), 0, Long
                                        .parseLong(tempObj.getString("userId")), 0,
                                        tempObj.optString("gravatarMd5", "")

                                )

                        );

                    }

                    // Sort it out
                    Collections.sort(profileRowRequests,
                            new ProfileComparator());

                }

                if (numFriends > 0) {

                    // Iterate baby!
                    for (int i = 0, max = friendsObject.length(); i < max; i++) {

                        // Grab the object
                        tempObj = friendsObject.optJSONObject(i);
                        presenceObj = tempObj.getJSONObject("presence");

                        // Save it
                        if (presenceObj.getBoolean("isOnline")) {

                            if (presenceObj.getBoolean("isPlaying")) {

                                profileRowPlaying.add(

                                        new ProfileData(

                                                tempObj.getString("username"), tempObj
                                                .getString("username"), 0,
                                                Long.parseLong(tempObj
                                                        .getString("userId")), 0,
                                                tempObj.optString("gravatarMd5", ""),
                                                true, true

                                        )

                                );

                            } else {

                                profileRowOnline.add(

                                        new ProfileData(

                                                tempObj.getString("username"), tempObj
                                                .getString("username"), 0,
                                                Long.parseLong(tempObj
                                                        .getString("userId")), 0,
                                                tempObj.optString("gravatarMd5", ""),
                                                true, false

                                        )

                                );

                            }

                        } else {

                            profileRowOffline.add(

                                    new ProfileData(

                                            tempObj.getString("username"), tempObj
                                            .getString("username"), 0, Long
                                            .parseLong(tempObj.getString("userId")), 0,
                                            tempObj.optString("gravatarMd5", "")

                                    )

                            );

                        }

                    }

                    // How many "online" friends do we have? Playing + idle
                    numFriends = profileRowPlaying.size()
                            + profileRowOnline.size();

                    // First add the separators)...
                    if (numFriends > 0) {

                        profileRowPlaying.add(

                                new ProfileData(

                                        "00000000", c
                                        .getString(R.string.info_txt_friends_online),
                                        0, 0, 0, null

                                )

                        );

                    }

                    if (profileRowOffline.size() > 0) {

                        profileRowOffline.add(

                                new ProfileData(

                                        "00000001", c
                                        .getString(R.string.info_txt_friends_offline),
                                        0, 0, 0, null

                                )

                        );

                    }

                    // ...then we sort it out...
                    Collections
                            .sort(profileRowPlaying, new ProfileComparator());
                    Collections.sort(profileRowOnline, new ProfileComparator());
                    Collections
                            .sort(profileRowOffline, new ProfileComparator());

                    // ...sprinkle a little merging here and there...
                    profileRowPlaying.addAll(profileRowOnline);

                }

                return new FriendListDataWrapper(profileRowRequests,
                        profileRowPlaying, profileRowOffline);

            } else {

                throw new WebsiteHandlerException(
                        "Could not retrieve the ProfileIDs.");

            }

        } catch (JSONException e) {

            throw new WebsiteHandlerException(e.getMessage());

        } catch (RequestHandlerException ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static boolean sendFriendRequest(long profileId, String checksum)
            throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent = wh.post(

                    Constants.URL_FRIEND_REQUESTS.replace(

                            "{UID}", profileId + ""

                    ), new PostData[]{

                    new PostData(

                            Constants.FIELD_NAMES_CHECKSUM[0], checksum

                    )
            }, 1

            );

            // Did we manage?
            if (httpContent != null) {

                return true;

            } else {

                return false;

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static boolean removeFriend(long profileId)
            throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent = wh.get(

                    Constants.URL_FRIEND_DELETE.replace("{UID}", profileId + ""), 1

            );

            // Did we manage?
            if (httpContent != null) {

                return true;

            } else {

                return false;

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static boolean answerFriendRequest(long pId, Boolean accepting,
                                              String checksum) throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent;

            // Get the content
            if (accepting) {

                httpContent = wh.post(

                        Constants.URL_FRIEND_ACCEPT.replace(

                                "{UID}", pId + ""), new PostData[]{

                        new PostData(

                                Constants.FIELD_NAMES_CHECKSUM[0], checksum

                        )

                }, 0

                );

            } else {

                httpContent = wh.post(

                        Constants.URL_FRIEND_DECLINE.replace(

                                "{UID}", pId + ""), new PostData[]{

                        new PostData(

                                Constants.FIELD_NAMES_CHECKSUM[0], checksum

                        )

                }, 0

                );

            }

            // Did we manage?
            if (!"".equals(httpContent)) {

                return true;

            } else {

                throw new WebsiteHandlerException(
                        "Could not respond to the friend request.");

            }

        } catch (RequestHandlerException ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static final ArrayList<ProfileData> getFriends(String checksum,
                                                          boolean noOffline) throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent;
            ArrayList<ProfileData> profileArray = new ArrayList<ProfileData>();

            // Get the content
            httpContent = wh.post(Constants.URL_FRIENDS,
                    new PostData[]{
                            new PostData(
                                    Constants.FIELD_NAMES_CHECKSUM[0], checksum)
                    }, 0);

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Generate an object
                JSONArray profileObject = new JSONObject(httpContent)
                        .getJSONObject("data").getJSONArray("friendscomcenter");
                JSONObject tempObj;

                // Iterate baby!
                for (int i = 0, max = profileObject.length(); i < max; i++) {

                    // Grab the object
                    tempObj = profileObject.optJSONObject(i);

                    // Only online friends?
                    if (noOffline
                            && !tempObj.getJSONObject("presence").getBoolean(
                            "isOnline")) {
                        continue;
                    }

                    // Save it
                    profileArray.add(

                            new ProfileData(

                                    tempObj.getString("username"), tempObj
                                    .getString("username"), 0, Long.parseLong(tempObj
                                    .getString("userId")), 0, tempObj.optString(
                                    "gravatarMd5", "")

                            )

                    );
                }

                return profileArray;

            } else {

                throw new WebsiteHandlerException(
                        "Could not retrieve the ProfileIDs.");

            }

        } catch (JSONException e) {

            throw new WebsiteHandlerException(e.getMessage());

        } catch (RequestHandlerException ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }
}