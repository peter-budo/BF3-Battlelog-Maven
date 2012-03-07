package com.ninetwozero.battlelog.services;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import com.ninetwozero.battlelog.R;
import com.ninetwozero.battlelog.datatypes.*;
import com.ninetwozero.battlelog.misc.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class PlatoonService {
    public static int applyForPlatoonMembership(final long platoonId,
                                                final String checksum) throws WebsiteHandlerException {

        try {

            // Let's set it up!
            RequestHandler wh = new RequestHandler();
            String httpContent;

            // Do the actual request
            httpContent = wh.post(

                    Constants.URL_PLATOON_APPLY, new PostData[]{

                    new PostData(Constants.FIELD_NAMES_PLATOON_APPLY[0],
                            platoonId + ""),
                    new PostData(Constants.FIELD_NAMES_PLATOON_APPLY[1],
                            checksum)

            }, 3

            );

            // What up?
            if (httpContent == null || httpContent.equals("")) {

                return -1; // Invalid request

            } else {

                if (httpContent.equals("success")) { // OK!

                    return 0;

                } else if (httpContent.equals("wrongplatform")) { // Wrong
                    // platform

                    return 1;

                } else if (httpContent.equals("maxmembersreached")) { // Full
                    // platoon

                    return 2;

                } else { // unknown

                    return 3;

                }

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static boolean closePlatoonMembership(final long platoonId,
                                                 final long userId, final String checksum)
            throws WebsiteHandlerException {

        try {

            // Let's set it up!
            RequestHandler wh = new RequestHandler();
            String httpContent;

            // Do the actual request
            httpContent = wh.post(

                    Constants.URL_PLATOON_LEAVE, new PostData[]{

                    new PostData(Constants.FIELD_NAMES_PLATOON_LEAVE[0],
                            platoonId + ""),
                    new PostData(Constants.FIELD_NAMES_PLATOON_LEAVE[1], userId
                            + ""),
                    new PostData(Constants.FIELD_NAMES_PLATOON_LEAVE[2],
                            checksum)

            }, 3

            );

            // What up?
            if (httpContent == null || httpContent.equals("")) {

                return false;

            } else {

                return true;

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static PlatoonData getPlatoonIdFromSearch(final String keyword,
                                                     final String checksum) throws WebsiteHandlerException {

        try {

            // Let's do this!
            RequestHandler wh = new RequestHandler();
            String httpContent;
            PlatoonData platoon = null;

            // Get the content
            httpContent = wh.post(

                    Constants.URL_PLATOON_SEARCH, new PostData[]{

                    new PostData(Constants.FIELD_NAMES_PLATOON_SEARCH[0],
                            keyword),
                    new PostData(Constants.FIELD_NAMES_PLATOON_SEARCH[1],
                            checksum)

            }, 3

            );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Generate an object
                JSONArray searchResults = new JSONArray(httpContent);

                // Did we get any results?
                if (searchResults.length() > 0) {

                    // init cost counters
                    int costOld = 999, costCurrent = 0;

                    // Iterate baby!
                    for (int i = 0, max = searchResults.length(); i < max; i++) {

                        // Get the JSONObject
                        JSONObject tempObj = searchResults.optJSONObject(i);

                        // Is it visible?
                        if (tempObj.getBoolean("hidden")) {
                            continue;
                        }

                        // A perfect match?
                        if (tempObj.getString("name").equals(keyword)) {

                            platoon = new PlatoonData(Long.parseLong(tempObj
                                    .getString("id")),
                                    tempObj.getInt("fanCounter"),
                                    tempObj.getInt("memberCounter"),
                                    tempObj.getInt("platform"),
                                    tempObj.getString("name"),
                                    tempObj.getString("tag"), null, true);

                            break;

                        }

                        // Grab the "cost"
                        costCurrent = PublicUtils.getLevenshteinDistance(
                                keyword, tempObj.getString("name"));

                        // Somewhat of a match? Get the "best" one!
                        if (costOld > costCurrent) {

                            platoon = new PlatoonData(

                                    Long.parseLong(tempObj.getString("id")),
                                    tempObj.getInt("fanCounter"),
                                    tempObj.getInt("memberCounter"),
                                    tempObj.getInt("platform"),
                                    tempObj.getString("name"),
                                    tempObj.getString("tag"), null, true

                            );

                        }

                        // Shuffle!
                        costOld = costCurrent;

                    }

                    return platoon;

                }

                throw new WebsiteHandlerException("No platoons found.");

            } else {

                throw new WebsiteHandlerException(
                        "Could not retreive the ProfileIDs.");

            }

        } catch (Exception ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static boolean answerPlatoonRequest(long plId, long pId,
                                               Boolean accepting, String checksum) throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent;

            // Modifier
            int modifier = (accepting) ? 0 : 1;

            httpContent = wh
                    .post(

                            Constants.URL_PLATOON_RESPOND.replace(

                                    "{PLATOON_ID}", plId + ""),
                            new PostData[]{

                                    new PostData(

                                            Constants.FIELD_NAMES_PLATOON_RESPOND[0],
                                            ""

                                    ),
                                    new PostData(

                                            Constants.FIELD_NAMES_PLATOON_RESPOND[1],
                                            pId + ""

                                    ),
                                    new PostData(

                                            Constants.FIELD_NAMES_PLATOON_RESPOND[2],
                                            checksum

                                    ),
                                    new PostData(

                                            Constants.FIELD_NAMES_PLATOON_RESPOND[3 + modifier],
                                            Constants.FIELD_VALUES_PLATOON_RESPOND[3 + modifier]

                                    )

                            }, 0

                    );

            // Did we manage?
            if (!"".equals(httpContent)) {

                return true;

            } else {

                throw new WebsiteHandlerException(
                        "Could not respond to the platoon request.");

            }

        } catch (RequestHandlerException ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static ArrayList<PlatoonMemberData> getFansForPlatoon(
            final long platoonId) throws WebsiteHandlerException {

        try {

            // Let's go!
            RequestHandler rh = new RequestHandler();
            ArrayList<PlatoonMemberData> fans = new ArrayList<PlatoonMemberData>();
            String httpContent;

            // Do the request
            httpContent = rh.get(

                    Constants.URL_PLATOON_FANS.replace("{PLATOON_ID}", platoonId + ""),
                    1

            );

            // Let's start with the JSON shall we?
            JSONObject fanArray = new JSONObject(httpContent).getJSONObject(
                    "context").getJSONObject("fans");
            JSONArray fanIdArray = fanArray.names();
            JSONObject tempObject = null;

            // Iterate over the fans
            if (fanIdArray != null) {

                for (int i = 0, max = fanIdArray.length(); i < max; i++) {

                    // Grab the fan
                    tempObject = fanArray
                            .getJSONObject(fanIdArray.getString(i));

                    // Store him in the ArrayList
                    fans.add(

                            new PlatoonMemberData(

                                    tempObject.getString("username"), null, new long[]{
                                    Long
                                            .parseLong(tempObject.getString("userId"))
                            }, 0,
                                    new long[]{
                                            0
                                    }, tempObject.optString(
                                    "gravatarMd5", ""), 0)

                    );

                }

            }

            // Did we get more than 0?
            if (fans.size() > 0) {

                // Add a header just 'cause we can
                fans.add(

                        new PlatoonMemberData(

                                "00000000", new String[]{
                                "Loyal fans"
                        }, new long[]{
                                0
                        }, 0,
                                new long[]{
                                        0
                                }, null, 0

                        )

                );

                // a-z please!
                Collections.sort(fans, new ProfileComparator());

            }
            // Return
            return fans;

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }
    }

    public static PlatoonInformation getProfileInformationForPlatoon(

            final Context c, final PlatoonData pData, final int num, final long aPId

    ) throws WebsiteHandlerException {

        try {
            /*
             * TODO: WHY IS IT CRASHING? LOAD UP A 100 MEMBER PLATOON AND TRY TO
             * DEBUG
             */
            // Let's go!
            RequestHandler rh = new RequestHandler();
            ArrayList<FeedItem> feedItemArray = new ArrayList<FeedItem>();
            ArrayList<PlatoonMemberData> fans = new ArrayList<PlatoonMemberData>();
            ArrayList<PlatoonMemberData> members = new ArrayList<PlatoonMemberData>();
            ArrayList<ProfileData> friends = new ArrayList<ProfileData>();
            PlatoonStats stats = null;

            // Arrays to divide the users in
            ArrayList<PlatoonMemberData> founderMembers = new ArrayList<PlatoonMemberData>();
            ArrayList<PlatoonMemberData> adminMembers = new ArrayList<PlatoonMemberData>();
            ArrayList<PlatoonMemberData> regularMembers = new ArrayList<PlatoonMemberData>();
            ArrayList<PlatoonMemberData> invitedMembers = new ArrayList<PlatoonMemberData>();
            ArrayList<PlatoonMemberData> requestMembers = new ArrayList<PlatoonMemberData>();

            // Get the content
            String httpContent = rh.get(
                    Constants.URL_PLATOON.replace("{PLATOON_ID}", pData.getId()
                            + ""), 1);
            boolean isAdmin = false;
            boolean isMember = false;

            // Did we manage?
            if (!"".equals(httpContent)) {

                // JSON Objects
                JSONObject contextObject = new JSONObject(httpContent)
                        .optJSONObject("context");

                // Does the platoon exist?
                if (contextObject.isNull("platoon")) {
                    return null;
                }

                // Moar JSON
                JSONObject profileCommonObject = contextObject
                        .optJSONObject("platoon");
                JSONObject memberArray = profileCommonObject
                        .optJSONObject("members");
                // JSONArray feedItems = contextObject.getJSONArray( "feed" );
                JSONObject currItem;

                // Get the user's friends
                friends = FriendsListService.getFriends(

                        PreferenceManager.getDefaultSharedPreferences(c)
                                .getString(Constants.SP_BL_CHECKSUM, ""), false

                );

                // Let's iterate over the members
                JSONArray idArray = memberArray.names();
                for (int counter = 0, max = idArray.length(); counter < max; counter++) {

                    // Temporary var
                    PlatoonMemberData tempProfile;

                    // Get the current item
                    currItem = memberArray.optJSONObject(idArray
                            .getString(counter));

                    // Check the *rights* of the user
                    if (idArray.getString(counter).equals("" + aPId)) {

                        isMember = true;
                        if (currItem.getInt("membershipLevel") >= 128) {
                            isAdmin = true;
                        }
                    }

                    // If we have a persona >> add it to the members
                    if (!currItem.isNull("persona")) {

                        tempProfile = new PlatoonMemberData(

                                currItem.getJSONObject("user").getString("username"),
                                new String[]{
                                        currItem
                                                .getJSONObject("persona").getString(
                                                "personaName")
                                },
                                new long[]{
                                        Long.parseLong(currItem
                                                .getString("personaId"))
                                },
                                Long.parseLong(currItem.getString("userId")),
                                new long[]{
                                        profileCommonObject
                                                .getLong("platform")
                                },
                                currItem.optString("gravatarMd5", ""), currItem
                                .getJSONObject("user")
                                .getJSONObject("presence")
                                .getBoolean("isOnline"), currItem
                                .getJSONObject("user")
                                .getJSONObject("presence")
                                .getBoolean("isPlaying"),
                                currItem.getInt("membershipLevel")

                        );

                    } else {

                        continue;

                    }

                    // Add the user to the correct *part* of the (future) merged
                    // list
                    switch (currItem.getInt("membershipLevel")) {

                        case 1:
                            requestMembers.add(tempProfile);
                            break;

                        case 2:
                            invitedMembers.add(tempProfile);
                            break;

                        case 4:
                            regularMembers.add(tempProfile);
                            break;

                        case 128:
                            adminMembers.add(tempProfile);
                            break;

                        case 256:
                            founderMembers.add(tempProfile);
                            break;

                        default:
                            regularMembers.add(tempProfile);
                            break;
                    }

                }

                // Let's sort the members...
                Collections.sort(requestMembers, new ProfileComparator());
                Collections.sort(invitedMembers, new ProfileComparator());
                Collections.sort(regularMembers, new ProfileComparator());
                Collections.sort(adminMembers, new ProfileComparator());
                Collections.sort(founderMembers, new ProfileComparator());

                // ...and then merge them with their *labels*
                if (founderMembers.size() > 0) {

                    // Plural?
                    if (founderMembers.size() > 1) {

                        members.add(new PlatoonMemberData(
                                "00000000",
                                c.getString(R.string.info_platoon_member_founder_p),
                                0, 0, 0, null, 0));

                    } else {

                        members.add(new PlatoonMemberData(
                                "00000001",
                                c.getString(R.string.info_platoon_member_founder),
                                0, 0, 0, null, 0));

                    }

                    // Add them to the members
                    members.addAll(founderMembers);

                }

                if (adminMembers.size() > 0) {

                    // Plural?
                    if (adminMembers.size() > 1) {

                        members.add(new PlatoonMemberData(
                                "00000002",
                                c.getString(R.string.info_platoon_member_admin_p),
                                0, 0, 0, null, 0));

                    } else {

                        members.add(new PlatoonMemberData("00000003", c
                                .getString(R.string.info_platoon_member_admin),
                                0, 0, 0, null, 0));

                    }

                    // Add them to the members
                    members.addAll(adminMembers);

                }

                if (regularMembers.size() > 0) {

                    // Plural?
                    if (regularMembers.size() > 1) {

                        members.add(new PlatoonMemberData(
                                "00000004",
                                c.getString(R.string.info_platoon_member_regular_p),
                                0, 0, 0, null, 0));

                    } else {

                        members.add(new PlatoonMemberData(
                                "00000005",
                                c.getString(R.string.info_platoon_member_regular),
                                0, 0, 0, null, 0));

                    }

                    // Add them to the members
                    members.addAll(regularMembers);

                }

                // Is the user *admin* or higher?
                if (isAdmin) {

                    if (invitedMembers.size() > 0) {

                        // Just add them
                        members.add(new PlatoonMemberData(
                                "00000006",
                                c.getString(R.string.info_platoon_member_invited_label),
                                0, 0, 0, null, 0));
                        members.addAll(invitedMembers);

                    }

                    if (requestMembers.size() > 0) {

                        // Just add them
                        members.add(new PlatoonMemberData(
                                "00000007",
                                c.getString(R.string.info_platoon_member_requested_label),
                                0, 0, 0, null, 0));
                        members.addAll(requestMembers);

                    }

                }

                // Let's get 'em fans too
                fans = getFansForPlatoon(pData.getId());

                // Oh man, don't forget the stats!!!
                stats = getStatsForPlatoon(c, pData);

                // Parse the feed
                // feedItemArray = getFeedItemsFromJSON(c, feedItems, aPId);

                // Let's see
                for (int i = 0, max = Math.round(num / 10); i < max; i++) {

                    // Get the content, and create a JSONArray
                    String tempContent = rh.get(

                            Constants.URL_PLATOON_FEED.replace(

                                    "{PLATOON_ID}", pData.getId() + ""

                            ).replace(

                                    "{NUMSTART}", String.valueOf(i * 10)

                            ), 1

                    );
                    JSONArray jsonArray = new JSONObject(tempContent)
                            .getJSONObject("data").getJSONArray("feedEvents");

                    // Gather them
                    feedItemArray.addAll(FeedsService.getFeedItemsFromJSON(c,
                            jsonArray, aPId));

                }

                // Required
                long platoonId = Long.parseLong(profileCommonObject
                        .getString("id"));
                String filename = platoonId + ".jpeg";

                // Is the image already cached?
                Log.d(Constants.DEBUG_TAG, "filename => " + filename
                        + " (cached: " + CacheHandler.isCached(c, filename)
                        + ")");
                if (!CacheHandler.isCached(c, filename)) {

                    WebsiteHandler.cacheBadge(c,
                            profileCommonObject.getString("badgePath"),
                            filename, Constants.DEFAULT_BADGE_SIZE);

                }

                // Return it!
                PlatoonInformation platoonInformation = new PlatoonInformation(

                        platoonId, profileCommonObject.getLong("creationDate"),
                        profileCommonObject.getInt("platform"),
                        profileCommonObject.getInt("game"),
                        profileCommonObject.getInt("fanCounter"),
                        profileCommonObject.getInt("memberCounter"),
                        profileCommonObject.getInt("blazeClubId"),
                        profileCommonObject.getString("name"),
                        profileCommonObject.getString("tag"),
                        profileCommonObject.getString("presentation"),
                        PublicUtils.normalizeUrl(profileCommonObject.optString(
                                "website", "")),
                        !profileCommonObject.getBoolean("hidden"), isMember,
                        isAdmin,
                        profileCommonObject.getBoolean("allowNewMembers"),
                        feedItemArray, members, fans, friends, stats

                );

                // Let's log it
                if (CacheHandler.Platoon.insert(c, platoonInformation) == 0) {

                    CacheHandler.Platoon.update(c, platoonInformation);

                }

                // return
                return platoonInformation;

            } else {

                throw new WebsiteHandlerException("Could not get the platoon.");

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static PlatoonStats getStatsForPlatoon(final Context context,
                                                  final PlatoonData platoonData) throws WebsiteHandlerException {

        try {

            // Let's go!
            RequestHandler rh = new RequestHandler();

            // Get the content
            String httpContent = rh.get(

                    Constants.URL_PLATOON_STATS.replace(

                            "{PLATOON_ID}", platoonData.getId() + ""

                    ).replace(

                            "{PLATFORM_ID}", platoonData.getPlatformId() + ""

                    ), 1

            );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // JSON-base!!
                JSONObject baseObject = new JSONObject(httpContent)
                        .getJSONObject("data");

                // Wait, did I just see what I think I saw?
                if (baseObject.isNull("platoonPersonas")) {

                    // Do we have a platformId?
                    if (platoonData.getPlatformId() == 0) {

                        // Get the content
                        String tempHttpContent = rh.get(

                                Constants.URL_PLATOON.replace(

                                        "{PLATOON_ID}", platoonData.getId() + ""

                                ), 1

                        );

                        // Build an object
                        JSONObject tempPlatoonData = new JSONObject(
                                tempHttpContent).getJSONObject("context")
                                .getJSONObject("platoon");

                        // Return and reloop
                        return getStatsForPlatoon(

                                context,
                                new PlatoonData(

                                        platoonData.getId(), tempPlatoonData
                                        .getInt("fanCounter"), tempPlatoonData
                                        .getInt("memberCounter"),
                                        tempPlatoonData.getInt("platform"),
                                        tempPlatoonData.getString("name"),
                                        tempPlatoonData.getString("tag"),
                                        platoonData.getId() + ".jpeg",
                                        tempPlatoonData.getBoolean("hidden")

                                )

                        );

                    } else {

                        return null;

                    }

                }

                // Hold on...
                JSONObject memberStats = baseObject
                        .getJSONObject("memberStats");
                JSONObject personaList = baseObject
                        .optJSONObject("platoonPersonas");

                // JSON data-branches
                JSONObject objectGeneral = memberStats.getJSONObject("general");
                JSONObject objectKit = memberStats
                        .getJSONObject("kitsVehicles");
                JSONObject objectTop = memberStats.getJSONObject("topPlayers");
                JSONObject objectScore = objectKit.getJSONObject("score");
                JSONObject objectSPM = objectKit
                        .getJSONObject("scorePerMinute");
                JSONObject objectTime = objectKit.getJSONObject("time");
                JSONObject currObj = null;
                JSONObject currUser = null;
                JSONArray currObjNames = null;

                // Let's iterate and create the containers
                ArrayList<PlatoonStatsItem> arrayGeneral = new ArrayList<PlatoonStatsItem>();
                ArrayList<PlatoonStatsItem> arrayScore = new ArrayList<PlatoonStatsItem>();
                ArrayList<PlatoonStatsItem> arraySPM = new ArrayList<PlatoonStatsItem>();
                ArrayList<PlatoonStatsItem> arrayTime = new ArrayList<PlatoonStatsItem>();
                ArrayList<PlatoonTopStatsItem> arrayTop = new ArrayList<PlatoonTopStatsItem>();

                // More temp
                ArrayList<Integer> tempMid = new ArrayList<Integer>();
                String tempGravatarHash = null;

                // Get the *general* stats
                currObjNames = objectGeneral.names();
                int maxNames = currObjNames.length();

                // Got any?
                if (maxNames == 0) {
                    return null;
                }

                for (int i = 0; i < maxNames; i++) {

                    // Grab the current object
                    currObj = objectGeneral.getJSONObject(currObjNames
                            .getString(i));

                    // Is it the KD? The KD == DOUBLE
                    if (currObjNames.getString(i).equals("kd")) {

                        // Create a new "stats item"
                        arrayGeneral.add(

                                new PlatoonStatsItem(

                                        currObjNames.getString(i), currObj.getDouble("min"),
                                        currObj.getDouble("median"), currObj
                                        .getDouble("best"), currObj
                                        .getDouble("average"), null

                                )

                        );

                    } else {

                        // Create a new "stats item"
                        arrayGeneral.add(

                                new PlatoonStatsItem(

                                        currObjNames.getString(i), currObj.getInt("min"),
                                        currObj.getInt("median"), currObj
                                        .getInt("best"), currObj
                                        .getInt("average"), null

                                )

                        );

                    }

                }

                // Create a new "overall item"
                arrayScore.add(new PlatoonStatsItem(context
                        .getString(R.string.info_platoon_stats_overall), 0, 0,
                        0, 0, null));

                // Get the *kit* scores
                currObjNames = objectScore.names();
                for (int i = 0, max = currObjNames.length(); i < max; i++) {

                    // Grab the current object
                    currObj = objectScore.getJSONObject(currObjNames
                            .getString(i));

                    // Hmm, where is the userInfo?
                    if (currObj.has("bestPersonaId")) {
                        currUser = personaList.getJSONObject(currObj
                                .getString("bestPersonaId"));
                    } else {
                        currUser = personaList.getJSONObject(currObj
                                .getString("personaId"));
                    }

                    // Store the gravatar
                    tempGravatarHash = currUser.optString("gravatarMd5", "");

                    // Create a new "stats item"
                    arrayScore.add(

                            new PlatoonStatsItem(

                                    currObjNames.getString(i), currObj.getInt("min"), currObj
                                    .getInt("median"), currObj.getInt("best"), currObj
                                    .getInt("average"), new ProfileData(

                                    currUser.optString("username", ""), currUser.optString(
                                    "username", ""), Long.parseLong(currObj
                                    .optString(
                                            "bestPersonaId", "")), Long
                                    .parseLong(currUser
                                            .optString("userId", "0")), platoonData
                                    .getPlatformId(), tempGravatarHash

                            )

                            )

                    );

                    // Is it ! the first?
                    if (i > 0) {
                        tempMid.add(arrayScore.get(i + 1).getMid());
                    }

                    // Update the "overall"
                    arrayScore.get(0).add(arrayScore.get(i + 1));

                }
                Collections.sort(tempMid);
                arrayScore.get(0).setMid(
                        tempMid.get((int) Math.ceil(tempMid.size() / 2)));
                tempMid.clear();

                // Create a new "overall item"
                arraySPM.add(new PlatoonStatsItem(context
                        .getString(R.string.info_platoon_stats_overall), 0, 0,
                        0, 0, null));

                // Get the *kit* score/min
                currObjNames = objectSPM.names();
                for (int i = 0, max = currObjNames.length(); i < max; i++) {

                    // Grab the current object
                    currObj = objectSPM
                            .getJSONObject(currObjNames.getString(i));

                    // Hmm, where is the userInfo?
                    if (currObj.has("bestPersonaId")) {
                        currUser = personaList.getJSONObject(currObj
                                .getString("bestPersonaId"));
                    } else {
                        currUser = personaList.getJSONObject(currObj
                                .getString("personaId"));
                    }

                    // Store the gravatar
                    tempGravatarHash = currUser.optString("gravatarMd5", "");

                    // Create a new "stats item"
                    arraySPM.add(

                            new PlatoonStatsItem(

                                    currObjNames.getString(i), currObj.getInt("min"), currObj
                                    .getInt("median"), currObj.getInt("best"), currObj
                                    .getInt("average"), new ProfileData(

                                    currUser.optString("username", ""), currUser.optString(
                                    "username", ""), Long.parseLong(currObj
                                    .optString(
                                            "bestPersonaId", "")), Long
                                    .parseLong(currUser
                                            .optString("userId", "0")), platoonData
                                    .getPlatformId(), tempGravatarHash

                            )

                            )

                    );

                    // Is it ! the first?
                    if (i > 0) {
                        tempMid.add(arraySPM.get(0).getMid());
                    }

                    // Update the "overall"
                    arraySPM.get(0).add(arraySPM.get(i + 1));

                }
                Collections.sort(tempMid);
                arraySPM.get(0).setMid(
                        tempMid.get((int) Math.ceil(tempMid.size() / 2)));
                tempMid.clear();

                // Create a new "overall item"
                arrayTime.add(new PlatoonStatsItem(context
                        .getString(R.string.info_platoon_stats_overall), 0, 0,
                        0, 0, null));

                // Get the *kit* times
                currObjNames = objectTime.names();
                for (int i = 0, max = currObjNames.length(); i < max; i++) {

                    // Grab the current object
                    currObj = objectTime.getJSONObject(currObjNames
                            .getString(i));

                    // Hmm, where is the userInfo?
                    if (currObj.has("bestPersonaId")) {
                        currUser = personaList.getJSONObject(currObj
                                .getString("bestPersonaId"));
                    } else {
                        currUser = personaList.getJSONObject(currObj
                                .getString("personaId"));
                    }

                    // Store the gravatar
                    tempGravatarHash = currUser.optString("gravatarMd5", "");

                    // Create a new "stats item"
                    arrayTime.add(

                            new PlatoonStatsItem(

                                    currObjNames.getString(i), currObj.getInt("min"), currObj
                                    .getInt("median"), currObj.getInt("best"), currObj
                                    .getInt("average"), new ProfileData(

                                    currUser.optString("username", ""), currUser.optString(
                                    "username", ""), Long.parseLong(currObj
                                    .optString(
                                            "bestPersonaId", "")), Long
                                    .parseLong(currUser
                                            .optString("userId", "0")), platoonData
                                    .getPlatformId(), tempGravatarHash

                            )

                            )

                    );

                    // Is it ! the first?
                    if (i > 0) {
                        tempMid.add(arrayTime.get(0).getMid());
                    }

                    // Update the "overall"
                    arrayTime.get(0).add(arrayTime.get(i + 1));

                }
                Collections.sort(tempMid);
                arrayTime.get(0).setMid(
                        tempMid.get((int) Math.ceil(tempMid.size() / 2)));
                tempMid.clear();

                // Get the *top player* Tops
                PlatoonTopStatsItem highestSPM = null;
                currObjNames = objectTop.names();

                for (int i = 0, max = currObjNames.length(); i < max; i++) {

                    // Grab the current object
                    currObj = objectTop
                            .getJSONObject(currObjNames.getString(i));

                    // Hmm, where is the userInfo?
                    if (currObj.has("bestPersonaId")) {
                        currUser = personaList.getJSONObject(currObj
                                .getString("bestPersonaId"));
                    } else if (!currObj.getString("personaId").equals("0")) {

                        currUser = personaList.getJSONObject(currObj
                                .getString("personaId"));

                    } else {

                        // Create a new "stats item"
                        arrayTop.add(

                                new PlatoonTopStatsItem(

                                        "N/A", 0, null

                                )

                        );

                        // Continue
                        continue;

                    }

                    // Store the gravatar
                    tempGravatarHash = currUser.optString("gravatarMd5", "");
                    String filename = tempGravatarHash + ".png";

                    // Do we need to download a new image?
                    if (!CacheHandler.isCached(context, filename)) {

                        WebsiteHandler.cacheGravatar(context, filename,
                                Constants.DEFAULT_AVATAR_SIZE);

                    }

                    // Create a new "stats item"
                    arrayTop.add(

                            new PlatoonTopStatsItem(

                                    currObjNames.getString(i), currObj.getInt("spm"),
                                    new ProfileData(

                                            currUser.optString("username", ""), currUser
                                            .optString("username", ""), Long
                                            .parseLong(currObj.optString("personaId",
                                                    "")), Long.parseLong(currUser
                                            .optString("userId", "0")), platoonData
                                            .getPlatformId(), tempGravatarHash

                                    )

                            )

                    );

                    // Store it if it's the highest
                    if (highestSPM == null
                            || highestSPM.getSPM() < arrayTop.get(i).getSPM()) {

                        highestSPM = arrayTop.get(i);

                    }

                }

                // Set the best & sort
                arrayTop.add(

                        new PlatoonTopStatsItem(

                                "TOP", highestSPM.getSPM(), highestSPM.getProfile()

                        )

                );
                Collections.sort(arrayTop, new TopStatsComparator());

                // Return it now!!
                return new PlatoonStats(

                        platoonData.getName(), platoonData.getId(), arrayGeneral,
                        arrayTop, arrayScore, arraySPM, arrayTime

                );

            } else {

                return null;

            }

        } catch (JSONException ex) {

            ex.printStackTrace();
            return null;

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static boolean alterPlatoonMembership(final long userId,
                                                 final long platoonId, final int filter)
            throws WebsiteHandlerException {

        try {

            // Init!
            String url = "";

            // Let's see filter we have here
            if (filter == 0) {
                url = Constants.URL_PLATOON_PROMOTE.replace("{UID}",
                        userId + "").replace("{PLATOON_ID}", platoonId + "");
            } else if (filter == 1) {
                url = Constants.URL_PLATOON_DEMOTE
                        .replace("{UID}", userId + "").replace("{PLATOON_ID}",
                                platoonId + "");
            } else if (filter == 2) {
                url = Constants.URL_PLATOON_KICK.replace("{UID}", userId + "")
                        .replace("{PLATOON_ID}", platoonId + "");
            }

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent = wh.get(

                    url, 2

            );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Check the JSON
                String status = new JSONObject(httpContent).optString(
                        "message", "");
                if (status.equals("USER_PROMOTED")
                        || status.equals("USER_DEMOTED")
                        || status.equals("MEMBER_KICKED")) {

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

    public static int sendPlatoonInvite(final long[] userId,
                                        final long platoonId, final String checksum)
            throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler rh = new RequestHandler();
            int numUsers = userId.length;
            ArrayList<PostData> postData = new ArrayList<PostData>();

            // Set the first two fields
            postData.add(new PostData(Constants.FIELD_NAMES_PLATOON_INVITE[0],
                    platoonId + ""));
            postData.add(new PostData(Constants.FIELD_NAMES_PLATOON_INVITE[1],
                    checksum));

            // We need to construct the array
            for (int i = 2, max = (numUsers + 2); i < max; i++) {

                if (userId[i - 2] == 0) {
                    continue;
                }
                postData.add(new PostData(
                        Constants.FIELD_NAMES_PLATOON_INVITE[2], userId[i - 2]
                        + ""));

            }

            // Get the content
            String httpContent = rh.post(Constants.URL_PLATOON_INVITE,
                    postData, 2);

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Check the JSON
                JSONObject baseObject = new JSONObject(httpContent);
                JSONArray errors = (baseObject.isNull("errors")) ? null
                        : baseObject.optJSONArray("errors");
                int numErrors = (errors == null) ? 0 : errors.length();

                // Let's see what we got
                if (numErrors == 0) {

                    return 0;

                } else if (numErrors == numUsers) {

                    return 1;

                } else if (numErrors < numUsers) {

                    return 2;

                } else {

                    return -1;

                }

            } else {

                return -1;

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static ArrayList<PlatoonData> getPlatoonsForUser(
            final Context context, final String username)
            throws WebsiteHandlerException {

        // Inir
        RequestHandler rh = new RequestHandler();
        ArrayList<PlatoonData> platoons = new ArrayList<PlatoonData>();

        try {

            // Get the content
            String httpContent = rh.get(
                    Constants.URL_PROFILE_INFO.replace("{UNAME}", username), 1);

            // Is it ok?
            if (!"".equals(httpContent)) {

                // JSON
                JSONArray platoonArray = new JSONObject(httpContent)
                        .getJSONObject("context")
                        .getJSONObject("profileCommon")
                        .getJSONArray("platoons");

                // Validate the platoons
                if (platoonArray != null && platoonArray.length() > 0) {

                    // Iterate!!
                    for (int i = 0, max = platoonArray.length(); i < max; i++) {

                        // Get the current item
                        JSONObject currItem = platoonArray.getJSONObject(i);

                        // Let's cache the gravatar
                        String title = currItem.getString("id") + ".jpeg";

                        // Is it cached?
                        if (!CacheHandler.isCached(context, title)) {

                            WebsiteHandler.cacheBadge(

                                    context, currItem.getString("badgePath"), title,
                                    Constants.DEFAULT_BADGE_SIZE

                            );

                        }

                        // Add to the ArrayList
                        platoons.add(

                                new PlatoonData(

                                        Long.parseLong(currItem.getString("id")), currItem
                                        .getInt("fanCounter"), currItem
                                        .getInt("memberCounter"), currItem
                                        .getInt("platform"),
                                        currItem.getString("name"), currItem
                                        .getString("tag"), title, !currItem
                                        .getBoolean("hidden")

                                )

                        );
                    }

                }

                return platoons;

            } else {

                return null;

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }
}