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

package com.ninetwozero.battlelog.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ninetwozero.battlelog.R;
import com.ninetwozero.battlelog.datatypes.WebsiteHandlerException;
import com.ninetwozero.battlelog.misc.WebsiteHandler;

public class AsyncPlatoonRequest extends AsyncTask<Boolean, Integer, Integer> {

    // Attribute
    private Context context;
    private long profileId, platoonId;
    private String checksum;
    private boolean isJoinRequest;

    // Constructor
    public AsyncPlatoonRequest(Context c, long plId, long pId, String ch) {

        this.context = c;
        this.profileId = pId;
        this.platoonId = plId;
        this.checksum = ch;

    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Integer doInBackground(Boolean... arg0) {

        try {

            // Get the arg0
            isJoinRequest = arg0[0];

            // Let's get this!!
            if (isJoinRequest) {

                return WebsiteHandler.applyForPlatoonMembership(platoonId,
                        checksum);

            } else {

                return WebsiteHandler.closePlatoonMembership(platoonId,
                        profileId, checksum) ? 0 : -1;

            }

        } catch (WebsiteHandlerException e) {

            return -1;

        }

    }

    @Override
    protected void onPostExecute(Integer results) {

        // Let the user know and then refresh!
        if (isJoinRequest) {

            switch (results) {

                case -1:
                    Toast.makeText(context, R.string.msg_error, Toast.LENGTH_SHORT)
                            .show();

                case 0:
                    Toast.makeText(context, R.string.msg_prequest_ok,
                            Toast.LENGTH_SHORT).show();
                    break;

                case 1:
                    Toast.makeText(context, R.string.msg_prequest_fail_pres,
                            Toast.LENGTH_SHORT).show();
                    break;

                case 2:
                    Toast.makeText(context, R.string.msg_prequest_fail_full,
                            Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(context, R.string.msg_prequest_fail_error,
                            Toast.LENGTH_SHORT).show();
                    break;
            }

        } else {

            if (results == 0) {

                Toast.makeText(context, R.string.msg_platoon_left,
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(context, R.string.msg_platoon_left_fail,
                        Toast.LENGTH_SHORT).show();

            }
        }

        return;

    }

}
