/*
	This file is part of BF3 Battlelog

    BF3 Battlelog is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BF3 Battlelog is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 */

package com.ninetwozero.battlelog.misc;

import android.content.Context;
import android.graphics.Bitmap;
import com.ninetwozero.battlelog.datatypes.*;
import org.apache.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/* 
 * Methods of this class should be loaded in AsyncTasks, as they would probably lock up the GUI
 */

public class WebsiteHandler {

    // Let's have this one ready
    public static HashMap<String, Object> feedCache = new HashMap<String, Object>();

    public static String downloadZip(Context context, String url, String title)
            throws WebsiteHandlerException {
        try {
            // Attributes
            RequestHandler rh = new RequestHandler();
            // Get the *content*
            if (!rh.saveFileFromURI(Constants.URL_IMAGE_PACK, "", "imagepack-001.zip")) {
                return "<this is supposed to be the path to the zip>";
            } else {
                throw new WebsiteHandlerException("No zip found.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static Bitmap downloadGravatarToBitmap(String hash, int size)
            throws WebsiteHandlerException {

        try {

            // Any size requirements? Otherwise we just pick the standard number
            return new RequestHandler().getImageFromStream(

                    Constants.URL_GRAVATAR.replace(

                            "{hash}", hash

                            ).replace(

                                    "{size}", ((size > 0) ? size : Constants.DEFAULT_AVATAR_SIZE

                                            ) + ""

                            ).replace(

                                    "{default}", Constants.DEFAULT_AVATAR_SIZE + ""

                            ), true

                    );

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static boolean cacheGravatar(Context c, String h, int s) {

        try {

            // Let's set it up
            RequestHandler rh = new RequestHandler();

            // Get the external cache dir
            String cacheDir = PublicUtils.getCachePath(c);

            // How does it end?
            if (!cacheDir.endsWith("/")) {
                cacheDir += "/";
            }

            // Get the actual stream
            HttpEntity httpEntity = rh.getHttpEntity(

                    Constants.URL_GRAVATAR.replace("{hash}", h)
                            .replace("{size}", s + "")
                            .replace("{default}", s + ""), false

                    );

            // Init
            int bytesRead = 0;
            int offset = 0;
            int contentLength = (int) httpEntity.getContentLength();
            byte[] data = new byte[contentLength];

            // Build a path
            String filepath = cacheDir + h;

            // Handle the streams
            InputStream imageStream = httpEntity.getContent();
            BufferedInputStream in = new BufferedInputStream(imageStream);

            // Iterate
            while (offset < contentLength) {

                bytesRead = in.read(data, offset, data.length - offset);
                if (bytesRead == -1) {
                    break;
                }
                offset += bytesRead;

            }

            // Alright?
            if (offset != contentLength) {

                throw new IOException("Only read " + offset
                        + " bytes; Expected " + contentLength + " bytes");

            }

            // Close the stream
            in.close();
            FileOutputStream out = new FileOutputStream(filepath);
            out.write(data);
            out.flush();
            out.close();

            return true;

        } catch (Exception ex) {

            ex.printStackTrace();
            return false;

        }

    }

    public static boolean cacheBadge(Context c, String h, String fName, int s) {

        try {

            // Let's set it up
            RequestHandler rh = new RequestHandler();

            // Get the external cache dir
            String cacheDir = PublicUtils.getCachePath(c);

            // How does it end?
            if (!cacheDir.endsWith("/")) {
                cacheDir += "/";
            }

            // Get the actual stream
            HttpEntity httpEntity = rh.getHttpEntity(

                    Constants.URL_PLATOON_IMAGE.replace("{BADGE_PATH}", h), true

                    );

            // Init
            int bytesRead = 0;
            int offset = 0;
            int contentLength = (int) httpEntity.getContentLength();
            byte[] data = new byte[contentLength];

            // Handle the streams
            InputStream imageStream = httpEntity.getContent();
            BufferedInputStream in = new BufferedInputStream(imageStream);

            // Build a path
            String filepath = cacheDir + fName;

            // Iterate
            while (offset < contentLength) {

                bytesRead = in.read(data, offset, data.length - offset);
                if (bytesRead == -1) {
                    break;
                }
                offset += bytesRead;

            }

            // Alright?
            if (offset != contentLength) {

                throw new IOException("Only read " + offset
                        + " bytes; Expected " + contentLength + " bytes");

            }

            // Close the in-stream, start the outbound
            in.close();
            FileOutputStream out = new FileOutputStream(filepath);
            out.write(data);
            out.flush();
            out.close();

            return true;

        } catch (Exception ex) {

            ex.printStackTrace();
            return false;

        }

    }

    //
    public static ArrayList<GeneralSearchResult> searchBattlelog(

            Context context, String keyword, String checksum

            ) throws WebsiteHandlerException {

        // Init
        ArrayList<GeneralSearchResult> results = new ArrayList<GeneralSearchResult>();
        RequestHandler rh = new RequestHandler();

        try {

            // Get the content
            String httpContent = rh.post(

                    Constants.URL_PROFILE_SEARCH, new PostData[] {

                            new PostData(Constants.FIELD_NAMES_PROFILE_SEARCH[0],
                                    keyword),
                            new PostData(Constants.FIELD_NAMES_PROFILE_SEARCH[1],
                                    checksum)

                    }, 0

                    );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Generate an object
                JSONArray searchResultsProfile = new JSONObject(httpContent)
                        .getJSONObject("data").getJSONArray("matches");

                // Did we get any results?
                if (searchResultsProfile.length() > 0) {

                    // Iterate over the results
                    for (int i = 0, max = searchResultsProfile.length(); i < max; i++) {

                        // Get the JSONObject
                        JSONObject tempObj = searchResultsProfile
                                .optJSONObject(i);
                        String gravatarHash = tempObj.optString("gravatarMd5",
                                "");

                        // Save it into an array
                        results.add(

                                new GeneralSearchResult(

                                        new ProfileData(

                                                tempObj.getString("username"), tempObj
                                                        .getString("username"), 0, Long
                                                        .parseLong(tempObj.getString("userId")), 0,
                                                gravatarHash

                                        )

                                )

                                );

                        // Construct the avatar
                        /*
                         * final String filename = gravatar+ ".png";
                         * Log.d(Constants.DEBUG_TAG, filename + " is cached: "
                         * + CacheHandler.isCached( context, filename ) ); //Do
                         * we need to download it? if( !CacheHandler.isCached(
                         * context, filename ) ) { WebsiteHandler.cacheGravatar(
                         * context, filename, Constants.DEFAULT_AVATAR_SIZE ); }
                         */
                    }

                }

            }

            // Get the content
            httpContent = rh.post(

                    Constants.URL_PLATOON_SEARCH, new PostData[] {

                            new PostData(Constants.FIELD_NAMES_PLATOON_SEARCH[0],
                                    keyword),
                            new PostData(Constants.FIELD_NAMES_PLATOON_SEARCH[1],
                                    checksum)

                    }, 3

                    );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Generate an object
                JSONArray searchResultsPlatoon = new JSONArray(httpContent);

                // Did we get any results?
                if (searchResultsPlatoon.length() > 0) {

                    // Iterate baby!
                    for (int i = 0, max = searchResultsPlatoon.length(); i < max; i++) {

                        // Get the JSONObject
                        JSONObject tempObj = searchResultsPlatoon
                                .optJSONObject(i);
                        final String filename = tempObj.getString("id")
                                + ".jpeg";

                        // Add it to the ArrayList
                        results.add(

                                new GeneralSearchResult(

                                        new PlatoonData(

                                                Long.parseLong(tempObj.getString("id")), tempObj
                                                        .getInt("fanCounter"), tempObj
                                                        .getInt("memberCounter"), tempObj
                                                        .getInt("platform"), tempObj
                                                        .getString("name"),
                                                tempObj.getString("tag"), filename, true

                                        )

                                )

                                );

                        /*
                         * Log.d(Constants.DEBUG_TAG, filename + " is cached: "
                         * + CacheHandler.isCached( context, filename ) ); //Do
                         * we need to download it? if( !CacheHandler.isCached(
                         * context, filename ) ) { WebsiteHandler.cacheBadge(
                         * context, tempObj.getString( "badgePath" ), filename,
                         * Constants.DEFAULT_BADGE_SIZE ); }
                         */

                    }

                }

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new WebsiteHandlerException(ex.getMessage());

        }

        // Return the results
        return results;

    }

}
