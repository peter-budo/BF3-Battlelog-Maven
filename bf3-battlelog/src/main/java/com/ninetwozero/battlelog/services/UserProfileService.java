package com.ninetwozero.battlelog.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.ninetwozero.battlelog.R;
import com.ninetwozero.battlelog.datatypes.*;
import com.ninetwozero.battlelog.misc.*;
import net.sf.andhsli.hotspotlogin.SimpleCrypto;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class UserProfileService {
    public static ProfileData doLogin(final Context context,
                                      final PostData[] postDataArray, final boolean savePassword)
            throws WebsiteHandlerException {

        // Init
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor spEdit = sharedPreferences.edit();
        String[] tempString = new String[10];
        String httpContent = "";
        ProfileData profile = null;

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            httpContent = wh.post(Constants.URL_LOGIN, postDataArray, 0);

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Set the int
                int startPosition = httpContent
                        .indexOf(Constants.ELEMENT_UID_LINK);
                String[] bits;

                // Did we find it?
                if (startPosition == -1) {

                    // Update the position
                    startPosition = httpContent
                            .indexOf(Constants.ELEMENT_ERROR_MESSAGE);

                    // Is it -1 again?
                    if (startPosition == -1) {

                        throw new WebsiteHandlerException(
                                "The website won't let us in. Please try again later.");

                    } else {

                        tempString[0] = httpContent.substring(startPosition)
                                .replace("</div>", "")
                                .replace(Constants.ELEMENT_ERROR_MESSAGE, "");
                        Toast.makeText(context, tempString[0] + " ",
                                Toast.LENGTH_SHORT).show();

                    }

                    return null;

                }

                // Cut out the appropriate bits (<a class="SOME CLASS HERE"
                // href="A LONG LINK HERE">NINETWOZERO
                tempString[0] = httpContent.substring(startPosition);
                tempString[0] = tempString[0].substring(0,
                        tempString[0].indexOf("\">"));
                bits = TextUtils.split(
                        tempString[0].replace(Constants.ELEMENT_UID_LINK, ""),
                        "/");

                // Get the checksum
                tempString[1] = httpContent.substring(httpContent
                        .indexOf(Constants.ELEMENT_STATUS_CHECKSUM));
                tempString[1] = tempString[1].substring(0,
                        tempString[1].indexOf("\" />")).replace(
                        Constants.ELEMENT_STATUS_CHECKSUM, "");

                // Let's work on getting the "username", not persona name -->
                // profileId
                tempString[2] = httpContent.substring(httpContent
                        .indexOf(Constants.ELEMENT_USERNAME_LINK));
                tempString[2] = tempString[2].substring(0,
                        tempString[2].indexOf("/\">")).replace(
                        Constants.ELEMENT_USERNAME_LINK, "");
                profile = getProfileIdFromSearch(tempString[2],
                        tempString[1]);
                profile = getPersonaIdFromProfile(profile);

                // Further more, we would actually like to store the userid and
                // name
                spEdit.putString(Constants.SP_BL_EMAIL,
                        postDataArray[0].getValue());

                // Should we remember the password?
                if (savePassword) {

                    spEdit.putString(Constants.SP_BL_PASSWORD, SimpleCrypto
                            .encrypt(postDataArray[0].getValue(),
                                    postDataArray[1].getValue()));
                    spEdit.putBoolean(Constants.SP_BL_REMEMBER, true);

                } else {

                    spEdit.putString(Constants.SP_BL_PASSWORD, "");
                    spEdit.putBoolean(Constants.SP_BL_REMEMBER, false);
                }

                // Init the strings
                String personaNames = "";
                String personaIds = "";
                String platformIds = "";

                // We need to append the different parts to the ^ strings
                for (int i = 0, max = profile.getPersonaNameArray().length; i < max; i++) {

                    personaNames += profile.getPersonaName(i) + ":";
                    personaIds += String.valueOf(profile.getPersonaId(i)) + ":";
                    platformIds += String.valueOf(profile.getPlatformId(i))
                            + ":";

                }

                // This we keep!!!
                spEdit.putString(Constants.SP_BL_USERNAME, tempString[2]);
                spEdit.putString(Constants.SP_BL_PERSONA, personaNames);
                spEdit.putLong(Constants.SP_BL_PROFILE_ID,
                        profile.getProfileId());
                spEdit.putString(Constants.SP_BL_PERSONA_ID, personaIds);
                spEdit.putString(Constants.SP_BL_PLATFORM_ID, platformIds);
                spEdit.putString(Constants.SP_BL_CHECKSUM, tempString[1]);

                // Cookie-related
                ArrayList<ShareableCookie> sca = RequestHandler.getCookies();
                if (sca != null) {

                    ShareableCookie sc = sca.get(0);
                    spEdit.putString(Constants.SP_BL_COOKIE_NAME, sc.getName());
                    spEdit.putString(Constants.SP_BL_COOKIE_VALUE,
                            sc.getValue());

                } else {

                    throw new WebsiteHandlerException(
                            context.getString(R.string.info_login_lostcookie));

                }

                // Co-co-co-commit
                spEdit.commit();

                // Do we want to start a service?
                int serviceInterval = sharedPreferences.getInt(
                        Constants.SP_BL_INTERVAL_SERVICE,
                        (Constants.HOUR_IN_SECONDS / 2)) * 1000;
                AlarmManager alarmManager = (AlarmManager) context
                        .getSystemService(Context.ALARM_SERVICE);
                alarmManager.setInexactRepeating(

                        AlarmManager.ELAPSED_REALTIME, 0, serviceInterval,
                        PendingIntent.getService(context, 0, new Intent(
                                context, BattlelogService.class), 0)

                );

                Log.d(Constants.DEBUG_TAG,
                        "Setting the service to update every "
                                + serviceInterval / 60000 + " minutes");

                // Return it!!
                return profile;

            } else {

                return null;

            }

        } catch (RequestHandlerException ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("Failed to log-in.");

        }

    }

    public static ProfileData getProfileIdFromSearch(final String keyword,
                                                     final String checksum) throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent;
            ProfileData profile = null;

            // Get the content
            httpContent = wh.post(

                    Constants.URL_PROFILE_SEARCH, new PostData[]{

                    new PostData(Constants.FIELD_NAMES_PROFILE_SEARCH[0],
                            keyword),
                    new PostData(Constants.FIELD_NAMES_PROFILE_SEARCH[1],
                            checksum)

            }, 0

            );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Generate an object
                JSONArray searchResults = new JSONObject(httpContent)
                        .getJSONObject("data").getJSONArray("matches");

                // Did we get any results?
                if (searchResults.length() > 0) {

                    // init cost counters
                    int costOld = 999, costCurrent = 0;

                    // Iterate baby!
                    for (int i = 0, max = searchResults.length(); i < max; i++) {

                        // Get the JSONObject
                        JSONObject tempObj = searchResults.optJSONObject(i);

                        // A perfect match?
                        if (tempObj.getString("username").equals(keyword)) {

                            profile = new ProfileData(
                                    tempObj.getString("username"),
                                    tempObj.getString("username"),
                                    0,
                                    Long.parseLong(tempObj.getString("userId")),
                                    0, tempObj.optString("gravatarMd5", "")

                            );

                            break;

                        }

                        // Grab the "cost"
                        costCurrent = PublicUtils.getLevenshteinDistance(
                                keyword, tempObj.getString("username"));

                        // Somewhat of a match? Get the "best" one!
                        if (costOld > costCurrent) {

                            profile = new ProfileData(

                                    tempObj.getString("username"),
                                    tempObj.getString("username"),
                                    0,
                                    Long.parseLong(tempObj.getString("userId")),
                                    0, tempObj.optString("gravatarMd5", "")

                            );

                        }

                        // Shuffle!
                        costOld = costCurrent;

                    }

                    return getPersonaIdFromProfile(profile);

                }

                return null;

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

    public static ProfileData getProfileIdFromPersona(final long personaId)
            throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent;

            // Get the content
            httpContent = wh.get(
                    Constants.URL_STATS_OVERVIEW.replace("{PID}",
                            personaId + "").replace("{PLATFORM_ID}", "0"), 0);

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Grab the array
                JSONObject user = new JSONObject(httpContent).getJSONObject(
                        "data").getJSONObject("user");

                return new ProfileData(

                        user.getString("username"), user.getString("username"), 0,
                        Long.parseLong(user.getString("userId")), 0,
                        user.getString("gravatarMd5")

                );

            } else {

                throw new WebsiteHandlerException(
                        "Could not retrieve the Profile.");

            }

        } catch (Exception ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static ProfileData getPersonaIdFromProfile(final long profileId)
            throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent;

            // Get the content
            httpContent = wh.get(
                    Constants.URL_PROFILE.replace("{UID}", profileId + ""), 0);

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Grab the array
                JSONArray soldierBox = new JSONObject(httpContent)
                        .getJSONObject("data").getJSONArray("soldiersBox");

                // Get the number of soldiers
                final int numPersonas = soldierBox.length();

                // Init arrays
                String[] personaNameArray = new String[numPersonas];
                long[] personaIdArray = new long[numPersonas];
                long[] platformIdArray = new long[numPersonas];

                // Loop
                for (int i = 0; i < numPersonas; i++) {

                    // Current soldier
                    JSONObject personaObject = soldierBox.optJSONObject(i)
                            .getJSONObject("persona");

                    // Grab the variable data
                    personaNameArray[i] = personaObject
                            .getString("personaName");
                    personaIdArray[i] = personaObject.getLong("personaId");
                    platformIdArray[i] = DataBank
                            .getPlatformIdFromName(personaObject
                                    .getString("namespace"));

                }

                return new ProfileData(

                        personaNameArray[0], personaNameArray, personaIdArray,
                        profileId, platformIdArray, null

                );

            } else {

                throw new WebsiteHandlerException(
                        "Could not retrieve the PersonaID.");

            }

        } catch (Exception ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static ProfileData getPersonaIdFromProfile(final ProfileData p)
            throws WebsiteHandlerException {

        try {

            ProfileData profile = getPersonaIdFromProfile(p
                    .getProfileId());
            return new ProfileData(

                    p.getAccountName(), profile.getPersonaNameArray(),
                    profile.getPersonaIdArray(), p.getProfileId(),
                    profile.getPlatformIdArray(), profile.getGravatarHash()

            );

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException("No profile found");

        }
    }

    public static PersonaStats getStatsForPersona(ProfileData pd)
            throws WebsiteHandlerException {

        try {

            // Do we have a personaId?
            if (pd.getPersonaId() == 0) {

                pd = getPersonaIdFromProfile(pd.getProfileId());

            }

            // Let's see...
            RequestHandler wh = new RequestHandler();

            // Get the data
            String content = wh.get(

                    Constants.URL_STATS_OVERVIEW.replace(

                            "{PID}", pd.getPersonaId(0) + ""

                            ).replace(

                                    "{PLATFORM_ID}", pd.getPlatformId(0) + ""), 0

                    );

            // JSON Objects
            JSONObject dataObject = new JSONObject(content)
                    .getJSONObject("data");

            // Is overviewStats NULL? If so, no data.
            if (dataObject.isNull("overviewStats")) {
                return null;
            }

            // Keep it up!
            JSONObject statsOverview = dataObject
                    .getJSONObject("overviewStats");
            JSONObject kitScores = statsOverview.getJSONObject("kitScores");
            JSONObject nextRankInfo = dataObject.getJSONObject("rankNeeded");
            JSONObject currRankInfo = dataObject
                    .getJSONObject("currentRankNeeded");

            // Yay
            return new PersonaStats(

                    pd.getAccountName(), pd.getPersonaName(0),
                    currRankInfo.getString("name"),
                    statsOverview.getLong("rank"), pd.getPersonaId(0),
                    pd.getProfileId(), pd.getPlatformId(0),
                    statsOverview.getLong("timePlayed"),
                    currRankInfo.getLong("pointsNeeded"),
                    nextRankInfo.getLong("pointsNeeded"),
                    statsOverview.getInt("kills"),
                    statsOverview.getInt("killAssists"),
                    statsOverview.getInt("vehiclesDestroyed"),
                    statsOverview.getInt("vehiclesDestroyedAssists"),
                    statsOverview.getInt("heals"),
                    statsOverview.getInt("revives"),
                    statsOverview.getInt("repairs"),
                    statsOverview.getInt("resupplies"),
                    statsOverview.getInt("deaths"),
                    statsOverview.getInt("numWins"),
                    statsOverview.getInt("numLosses"),
                    statsOverview.getDouble("kdRatio"),
                    statsOverview.getDouble("accuracy"),
                    statsOverview.getDouble("longestHeadshot"),
                    statsOverview.getDouble("killStreakBonus"),
                    statsOverview.getDouble("elo"),
                    statsOverview.getDouble("scorePerMinute"),
                    kitScores.getLong("1"), kitScores.getLong("2"),
                    kitScores.getLong("32"), kitScores.getLong("8"),
                    statsOverview.getLong("sc_vehicle"),
                    statsOverview.getLong("combatScore"),
                    statsOverview.getLong("sc_award"),
                    statsOverview.getLong("sc_unlock"),
                    statsOverview.getLong("totalScore")

            );

        } catch (Exception ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static HashMap<Long, PersonaStats> getStatsForUser(
            final Context context, final ProfileData pd)
            throws WebsiteHandlerException {

        try {

            // Init
            HashMap<Long, PersonaStats> stats = new HashMap<Long, PersonaStats>();
            ProfileData profileData = pd;

            // Do we have a personaId?
            if (profileData.getPersonaId() == 0) {

                profileData = getPersonaIdFromProfile(pd.getProfileId());

            }

            // Let's see...
            RequestHandler wh = new RequestHandler();
            for (int i = 0, max = profileData.getNumPersonas(); i < max; i++) {

                // Get the data
                String httpContent = wh.get(

                        Constants.URL_STATS_OVERVIEW.replace(

                                "{PID}", profileData.getPersonaId(i) + ""

                                ).replace(

                                        "{PLATFORM_ID}", profileData.getPlatformId(i) + ""), 0

                        );

                // JSON Objects
                JSONObject dataObject = new JSONObject(httpContent)
                        .getJSONObject("data");

                // Is overviewStats NULL? If so, no data.
                if (!dataObject.isNull("overviewStats")) {

                    // Keep it up!
                    JSONObject statsOverview = dataObject
                            .getJSONObject("overviewStats");
                    JSONObject kitScores = statsOverview
                            .getJSONObject("kitScores");
                    JSONObject nextRankInfo = dataObject
                            .getJSONObject("rankNeeded");
                    JSONObject currRankInfo = dataObject
                            .getJSONObject("currentRankNeeded");

                    // Yay
                    stats.put(

                            profileData.getPersonaId(i),
                            new PersonaStats(

                                    profileData.getAccountName(), profileData
                                            .getPersonaName(i), currRankInfo
                                            .getString("name"), statsOverview
                                            .getLong("rank"), profileData
                                            .getPersonaId(i), profileData
                                            .getProfileId(), profileData
                                            .getPlatformId(i), statsOverview
                                            .getLong("timePlayed"), currRankInfo
                                            .getLong("pointsNeeded"), nextRankInfo
                                            .getLong("pointsNeeded"), statsOverview
                                            .getInt("kills"), statsOverview
                                            .getInt("killAssists"), statsOverview
                                            .getInt("vehiclesDestroyed"), statsOverview
                                            .getInt("vehiclesDestroyedAssists"),
                                    statsOverview.getInt("heals"),
                                    statsOverview.getInt("revives"),
                                    statsOverview.getInt("repairs"),
                                    statsOverview.getInt("resupplies"),
                                    statsOverview.getInt("deaths"),
                                    statsOverview.getInt("numWins"),
                                    statsOverview.getInt("numLosses"),
                                    statsOverview.getDouble("kdRatio"),
                                    statsOverview.getDouble("accuracy"),
                                    statsOverview.getDouble("longestHeadshot"),
                                    statsOverview.getDouble("killStreakBonus"),
                                    statsOverview.getDouble("elo"),
                                    statsOverview.getDouble("scorePerMinute"),
                                    kitScores.getLong("1"), kitScores
                                            .getLong("2"), kitScores
                                            .getLong("32"), kitScores
                                            .getLong("8"), statsOverview
                                            .getLong("sc_vehicle"),
                                    statsOverview.getLong("combatScore"),
                                    statsOverview.getLong("sc_award"),
                                    statsOverview.getLong("sc_unlock"),
                                    statsOverview.getLong("totalScore")

                            )

                            );

                } else {

                    stats.put(

                            profileData.getPersonaId(i),
                            new PersonaStats(

                                    profileData.getAccountName(), profileData
                                            .getPersonaName(i), "Recruit", 0,
                                    profileData.getPersonaId(i), profileData
                                            .getProfileId(), profileData
                                            .getPlatformId(i), 0, 0, 0, 0, 0,
                                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0, 0.0, 0.0,
                                    0.0, 0.0, 0.0, 0, 0, 0, 0, 0, 0, 0, 0, 0

                            )

                            );

                }

            }

            if (CacheHandler.Persona.insert(context, stats) == 0) {

                CacheHandler.Persona.update(context, stats);

            }

            // Return!
            return stats;

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static HashMap<Long, UnlockDataWrapper> getUnlocksForUser(
            final ProfileData pd) throws WebsiteHandlerException {

        try {

            // Init the RequestHandler
            RequestHandler wh = new RequestHandler();

            // Init the ArrayLists
            HashMap<Long, UnlockDataWrapper> unlockDataMap = new HashMap<Long, UnlockDataWrapper>();
            ArrayList<UnlockData> weaponArray;
            ArrayList<UnlockData> attachmentArray;
            ArrayList<UnlockData> kitUnlockArray;
            ArrayList<UnlockData> vehicleUpgradeArray;
            ArrayList<UnlockData> skillArray;
            ArrayList<UnlockData> unlockArray;

            for (int count = 0, maxCount = pd.getNumPersonas(); count < maxCount; count++) {

                weaponArray = new ArrayList<UnlockData>();
                attachmentArray = new ArrayList<UnlockData>();
                kitUnlockArray = new ArrayList<UnlockData>();
                vehicleUpgradeArray = new ArrayList<UnlockData>();
                skillArray = new ArrayList<UnlockData>();
                unlockArray = new ArrayList<UnlockData>();

                // Get the data
                String content = "";

                content = wh.get(

                        Constants.URL_STATS_UNLOCKS.replace(

                                "{PID}", pd.getPersonaId(count) + ""

                                ).replace(

                                        "{PLATFORM_ID}", pd.getPlatformId(count) + ""), 0

                        );

                // JSON Objects
                JSONObject dataObject = new JSONObject(content)
                        .getJSONObject("data");
                JSONArray unlockResults = dataObject.optJSONArray("unlocks");
                int unlockKit;

                if (dataObject.isNull("unlocks") || unlockResults.length() == 0) {

                    unlockDataMap.put(pd.getPersonaId(count),
                            new UnlockDataWrapper(null, null, null, null, null,
                                    null));
                    continue;

                }

                // Iterate over the unlocksArray
                for (int i = 0, max = unlockResults.length(); i < max; i++) {

                    // Get the temporary object
                    JSONObject unlockRow = unlockResults.optJSONObject(i);

                    try {

                        unlockKit = unlockRow.getInt("kit");

                    } catch (Exception ex) {

                        unlockKit = 0;

                    }

                    // What did we get?
                    if (!unlockRow.isNull("weaponAddonUnlock")) {

                        // Get the object
                        JSONObject detailObject = unlockRow
                                .getJSONObject("weaponAddonUnlock");
                        JSONObject unlockDetails = detailObject
                                .getJSONObject("unlockedBy");

                        // Bigger than 0.0 (ie 0.1+)?
                        if (unlockDetails.getDouble("completion") == 0.0) {
                            continue;
                        }

                        // Add them to the array
                        attachmentArray.add(

                                new UnlockData(

                                        unlockKit, unlockDetails.getDouble("completion"),
                                        unlockDetails.getLong("valueNeeded"),
                                        unlockDetails.getLong("actualValue"),
                                        detailObject.getString("weaponCode"),
                                        detailObject.getString("unlockId"),
                                        unlockDetails.getString("codeNeeded"),
                                        "weapon+"

                                )

                                );

                    } else if (!unlockRow.isNull("kitItemUnlock")) {

                        // Get the object
                        JSONObject detailObject = unlockRow
                                .getJSONObject("kitItemUnlock");
                        JSONObject unlockDetails = detailObject
                                .getJSONObject("unlockedBy");

                        // Less than 1.0?
                        if (unlockDetails.getDouble("completion") < 1.0) {
                            continue;
                        }

                        // Add them to the array
                        kitUnlockArray.add(

                                new UnlockData(

                                        unlockKit, unlockDetails.getDouble("completion"),
                                        unlockDetails.getLong("valueNeeded"),
                                        unlockDetails.getLong("actualValue"), unlockRow
                                                .getString("parentId"), detailObject
                                                .getString("unlockId"), unlockDetails
                                                .getString("codeNeeded"), "kit+"

                                )

                                );

                    } else if (!unlockRow.isNull("vehicleAddonUnlock")) {

                        // Get the object
                        JSONObject detailObject = unlockRow
                                .getJSONObject("vehicleAddonUnlock");
                        JSONObject unlockDetails = detailObject
                                .getJSONObject("unlockedBy");

                        // Less than 1.0?
                        if (unlockDetails.getDouble("completion") < 1.0) {
                            continue;
                        }

                        // Add them to the array
                        vehicleUpgradeArray.add(

                                new UnlockData(

                                        unlockKit, unlockDetails.getDouble("completion"),
                                        unlockDetails.getLong("valueNeeded"),
                                        unlockDetails.getLong("actualValue"), unlockRow
                                                .getString("parentId"), detailObject
                                                .getString("unlockId"), unlockDetails
                                                .getString("codeNeeded"), "vehicle+"

                                )

                                );

                    } else if (!unlockRow.isNull("weaponUnlock")) {

                        // Get the object
                        JSONObject detailObject = unlockRow
                                .getJSONObject("weaponUnlock");
                        JSONObject unlockDetails = detailObject
                                .getJSONObject("unlockedBy");

                        // Less than 1.0?
                        if (unlockDetails.getDouble("completion") < 1.0) {
                            continue;
                        }

                        // Add them to the array
                        weaponArray.add(

                                new UnlockData(

                                        unlockKit, unlockDetails.getDouble("completion"),
                                        unlockDetails.getLong("valueNeeded"),
                                        unlockDetails.getLong("actualValue"), unlockRow
                                                .getString("parentId"), detailObject
                                                .getString("unlockId"), unlockDetails
                                                .getString("codeNeeded"), "weapon"

                                )

                                );

                    } else if (!unlockRow.isNull("soldierSpecializationUnlock")) {

                        // Get the object
                        JSONObject detailObject = unlockRow
                                .getJSONObject("soldierSpecializationUnlock");
                        JSONObject unlockDetails = detailObject
                                .getJSONObject("unlockedBy");

                        // Less than 1.0?
                        if (unlockDetails.getDouble("completion") < 1.0) {
                            continue;
                        }

                        // Add them to the array
                        skillArray.add(

                                new UnlockData(

                                        unlockKit, unlockDetails.getDouble("completion"),
                                        unlockDetails.getLong("valueNeeded"),
                                        unlockDetails.getLong("actualValue"), unlockRow
                                                .getString("parentId"), detailObject
                                                .getString("unlockId"), unlockDetails
                                                .getString("codeNeeded"), "skill"

                                )

                                );

                    } else {
                    }
                }

                // Let's put them together
                unlockArray.addAll(weaponArray);
                unlockArray.addAll(attachmentArray);
                unlockArray.addAll(kitUnlockArray);
                unlockArray.addAll(vehicleUpgradeArray);
                unlockArray.addAll(skillArray);

                // Yay
                Collections.sort(weaponArray, new UnlockComparator());
                Collections.sort(attachmentArray, new UnlockComparator());
                Collections.sort(kitUnlockArray, new UnlockComparator());
                Collections.sort(vehicleUpgradeArray, new UnlockComparator());
                Collections.sort(skillArray, new UnlockComparator());
                Collections.sort(unlockArray, new UnlockComparator());

                unlockDataMap.put(pd.getPersonaId(count),
                        new UnlockDataWrapper(weaponArray, attachmentArray,
                                kitUnlockArray, vehicleUpgradeArray,
                                skillArray, unlockArray));

            }

            return unlockDataMap;

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static ProfileInformation getProfileInformationForUser(
            Context context, ProfileData profileData, int num,
            long activeProfileId) throws WebsiteHandlerException {

        try {

            // Let's go!
            RequestHandler rh = new RequestHandler();
            ArrayList<FeedItem> feedItemArray = new ArrayList<FeedItem>();
            ArrayList<PlatoonData> platoonDataArray = new ArrayList<PlatoonData>();
            String httpContent;

            // Get the content
            httpContent = rh.get(
                    Constants.URL_PROFILE_INFO.replace("{UNAME}",
                            profileData.getAccountName()), 1);

            // Did we manage?
            if (!"".equals(httpContent)) {

                // JSON Objects
                JSONObject contextObject = new JSONObject(httpContent)
                        .optJSONObject("context");
                JSONObject profileCommonObject = contextObject
                        .optJSONObject("profileCommon");
                JSONObject userInfo = profileCommonObject
                        .optJSONObject("userinfo");
                JSONObject presenceObject = profileCommonObject.getJSONObject(
                        "user").getJSONObject("presence");
                // JSONArray gameReports = contextObject.getJSONArray(
                // "gameReportPreviewGroups" );
                JSONArray soldierArray = contextObject
                        .getJSONArray("soldiersBox");
                JSONArray feedItems = contextObject.getJSONArray("feed");
                JSONArray platoonArray = profileCommonObject
                        .getJSONArray("platoons");
                JSONObject statusMessage = profileCommonObject
                        .optJSONObject("userStatusMessage");
                JSONObject currItem;
                String playingOn;

                // Persona related
                int numSoldiers = soldierArray.length();
                int numPlatoons = platoonArray.length();

                // Init the arrays
                String[] personaNameArray = new String[numSoldiers];
                long[] personaIdArray = new long[numSoldiers];
                long[] platformIdArray = new long[numSoldiers];
                long[] platoonIdArray = new long[numPlatoons];

                // Get the username
                String username = profileCommonObject.getJSONObject("user")
                        .getString("username");

                // Is status messages null?
                if (statusMessage == null) {
                    statusMessage = new JSONObject(
                            "{'statusMessage':'', 'statusMessageChanged':0}");
                }

                // What's up with the user?
                if (presenceObject.isNull("serverName")
                        && presenceObject.getBoolean("isPlaying")) {

                    playingOn = (presenceObject.getInt("platform") == 2) ? "Xbox Live"
                            : "Playstation Network";

                } else {

                    playingOn = presenceObject.optString("serverName", "");

                }

                for (int i = 0, max = numSoldiers; i < max; i++) {

                    // Each loop is an object
                    currItem = soldierArray.getJSONObject(i);
                    JSONObject personaObject = currItem
                            .getJSONObject("persona");

                    // Store them
                    personaIdArray[i] = Long.parseLong(personaObject
                            .getString("personaId"));
                    platformIdArray[i] = DataBank
                            .getPlatformIdFromName(personaObject
                                    .getString("namespace"));
                    personaNameArray[i] = (

                            (personaObject.isNull("personaName") ? username
                                    : personaObject.getString("personaName")) + " " + DataBank
                                    .resolvePlatformId((int) platformIdArray[i])

                            );

                }

                // Iterate over the platoons
                for (int i = 0; i < numPlatoons; i++) {

                    // Each loop is an object
                    currItem = platoonArray.getJSONObject(i);

                    // Store the id
                    platoonIdArray[i] = Long
                            .parseLong(currItem.getString("id"));

                    // Let's cache the gravatar
                    String title = currItem.getString("id") + ".jpeg";

                    // Log.d(Constants.DEBUG_TAG, "filename => " + title +
                    // " (cached: " + CacheHandler.isCached( context, title ) +
                    // ")");

                    // Is it cached?
                    if (!CacheHandler.isCached(context, title)) {

                        WebsiteHandler.cacheBadge(

                                context, currItem.getString("badgePath"), title,
                                Constants.DEFAULT_BADGE_SIZE

                        );

                    }

                    // Store the data
                    platoonDataArray.add(

                            new PlatoonData(

                                    Long.parseLong(currItem.getString("id")), currItem
                                            .getInt("fanCounter"), currItem
                                            .getInt("memberCounter"), currItem
                                            .getInt("platform"), currItem.getString("name"),
                                    currItem.getString("tag"), title, !currItem
                                            .getBoolean("hidden")

                            )

                            );

                }

                // Parse the feed
                feedItemArray = FeedsService.getFeedItemsFromJSON(context, feedItems,
                        activeProfileId);

                // Let's see
                for (int i = 1, max = Math.round(num / 10); i < max; i++) {

                    // Get the content, and create a JSONArray
                    String tempContent = rh.get(

                            Constants.URL_PROFILE_FEED.replace(

                                    "{PID}", profileData.getProfileId() + ""

                                    ).replace(

                                            "{NUMSTART}", String.valueOf(i * 10)

                                    ), 1

                            );
                    JSONArray jsonArray = new JSONObject(tempContent)
                            .getJSONObject("data").getJSONArray("feedEvents");

                    // Gather them
                    feedItemArray.addAll(FeedsService.getFeedItemsFromJSON(
                            context, jsonArray, activeProfileId));

                }

                ProfileInformation tempProfile = new ProfileInformation(

                        userInfo.optInt("age", 0), profileData.getProfileId(),
                        userInfo.optLong("birthdate", 0), userInfo.optLong(
                                "lastLogin", 0), statusMessage.optLong(
                                "statusMessageChanged", 0), personaIdArray,
                        platformIdArray, platformIdArray, userInfo.optString(
                                "name", "N/A"), username,
                        userInfo.isNull("presentation") ? null : userInfo
                                .getString("presentation"), userInfo.optString(
                                "location", "us"), statusMessage.optString(
                                "statusMessage", ""), playingOn,
                        personaNameArray, userInfo.optBoolean(
                                "allowFriendRequests", true),
                        presenceObject.getBoolean("isOnline"),
                        presenceObject.getBoolean("isPlaying"),
                        profileCommonObject.getString("friendStatus").equals(
                                "ACCEPTED"), feedItemArray, platoonDataArray);

                // Let's log it
                if (CacheHandler.Profile.insert(context, tempProfile) == 0) {

                    CacheHandler.Profile.update(context, tempProfile);

                }

                // RETURN
                return tempProfile;

            } else {

                throw new WebsiteHandlerException("Could not get the profile.");

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static HashMap<Long, ArrayList<AssignmentData>> getAssignments(
            Context c, ProfileData profile) throws WebsiteHandlerException {

        try {

            // Attributes
            RequestHandler rh = new RequestHandler();
            HashMap<Long, ArrayList<AssignmentData>> assignmentMap = new HashMap<Long, ArrayList<AssignmentData>>();
            ArrayList<AssignmentData> items;

            for (int count = 0, maxCount = profile.getNumPersonas(); count < maxCount; count++) {

                // Init
                items = new ArrayList<AssignmentData>();

                // Get the JSON!
                String httpContent = rh.get(

                        Constants.URL_STATS_ASSIGNMENTS.replace(

                                "{PNAME}", profile.getPersonaName(count)

                                ).replace(

                                        "{PID}", profile.getPersonaId(count) + ""

                                ).replace(

                                        "{UID}", profile.getProfileId() + ""

                                ).replace(

                                        "{PLATFORM_ID}", profile.getPlatformId(count) + ""), 1

                        );

                // Parse the JSON!
                JSONObject topLevel = new JSONObject(httpContent)
                        .getJSONObject("data");
                if (topLevel.isNull("missionTrees")) {

                    assignmentMap.put(profile.getPersonaId(count), items);
                    continue;

                }

                JSONObject missionTrees = topLevel
                        .getJSONObject("missionTrees");
                JSONArray missionLines = missionTrees.getJSONObject("512")
                        .getJSONArray("missionLines");
                int numCurrentAssignment = 0;
                for (int i = 0, max = missionLines.length(); i < max; i++) {

                    // Let's see if we need to tell the dev
                    if (max > Constants.ASSIGNMENT_RESOURCES_SCHEMATICS.length) {

                        Toast.makeText(

                                c, c.getString(R.string.info_assignments_new_unknown),
                                Toast.LENGTH_SHORT

                        ).show();

                        return null;

                    }

                    // Get the JSONObject per loop
                    JSONArray missions = missionLines.getJSONObject(i)
                            .getJSONArray("missions");
                    for (int missionCounter = 0, missionCount = missions
                            .length(); missionCounter < missionCount; missionCounter++) {

                        JSONObject assignment = missions
                                .getJSONObject(missionCounter);
                        JSONArray criteriasJSON = assignment
                                .getJSONArray("criterias");
                        JSONArray dependenciesJSON = assignment
                                .getJSONArray("dependencies");
                        JSONArray unlocksJSON = assignment
                                .getJSONArray("unlocks");

                        // Init
                        ArrayList<AssignmentData.Objective> criterias = new ArrayList<AssignmentData.Objective>();
                        ArrayList<AssignmentData.Dependency> dependencies = new ArrayList<AssignmentData.Dependency>();
                        ArrayList<AssignmentData.Unlock> unlocks = new ArrayList<AssignmentData.Unlock>();

                        // Alright, let's do this
                        for (int assignmentCounter = 0, assignmentCount = criteriasJSON
                                .length(); assignmentCounter < assignmentCount; assignmentCounter++) {

                            // Get the current item
                            JSONObject currentItem = criteriasJSON
                                    .getJSONObject(assignmentCounter);

                            // New object!
                            criterias.add(

                                    new AssignmentData.Objective(

                                            currentItem.getDouble("actualValue"), currentItem
                                                    .getDouble("completionValue"), currentItem
                                                    .getString("statCode"), currentItem
                                                    .getString("paramX"), currentItem
                                                    .getString("paramY"), currentItem
                                                    .getString("descriptionID"), currentItem
                                                    .getString("unit")

                                    )

                                    );

                        }

                        // Alright, let's do this
                        for (int counter = 0, maxCounter = dependenciesJSON
                                .length(); counter < maxCounter; counter++) {

                            // Get the current item
                            JSONObject currentItem = dependenciesJSON
                                    .getJSONObject(counter);

                            // New object!
                            dependencies.add(

                                    new AssignmentData.Dependency(

                                            currentItem.getInt("count"), currentItem
                                                    .getString("code")

                                    )

                                    );

                        }

                        // Alright, let's do this
                        for (int counter = 0, maxCounter = unlocksJSON.length(); counter < maxCounter; counter++) {

                            // Get the current item
                            JSONObject currentItem = unlocksJSON
                                    .getJSONObject(counter);

                            // New object!
                            unlocks.add(

                                    new AssignmentData.Unlock(

                                            currentItem.getString("unlockId"), currentItem
                                                    .getString("unlockType"), currentItem
                                                    .getBoolean("visible")

                                    )

                                    );

                        }

                        // Add the assignment
                        items.add(

                                new AssignmentData(

                                        Constants.ASSIGNMENT_RESOURCES_SCHEMATICS[numCurrentAssignment],
                                        Constants.ASSIGNMENT_RESOURCES_UNLOCKS[numCurrentAssignment],
                                        assignment.getString("stringID"), assignment
                                                .getString("descriptionID"), assignment
                                                .getString("license"), criterias,
                                        dependencies, unlocks

                                )

                                );

                        // Update the digit
                        numCurrentAssignment++;

                    }

                    // Add the items
                    assignmentMap.put(profile.getPersonaId(count), items);

                }

            }

            return assignmentMap;

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }
}