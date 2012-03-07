package com.ninetwozero.battlelog.services;

import com.ninetwozero.battlelog.datatypes.ChatMessage;
import com.ninetwozero.battlelog.datatypes.PostData;
import com.ninetwozero.battlelog.datatypes.RequestHandlerException;
import com.ninetwozero.battlelog.datatypes.WebsiteHandlerException;
import com.ninetwozero.battlelog.misc.Constants;
import com.ninetwozero.battlelog.misc.RequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatService {
    public static Long getChatId(long profileId, String checksum)
            throws WebsiteHandlerException {

        try {

            // Let's do this!
            RequestHandler wh = new RequestHandler();
            String httpContent;

            // Get the content
            httpContent = wh.post(

                    Constants.URL_CHAT_CONTENTS.replace(

                            "{UID}", profileId + ""), new PostData[]{

                    new PostData(

                            Constants.FIELD_NAMES_CHECKSUM[0], checksum

                    )

            }, 0

            );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Get the messages
                return new JSONObject(httpContent).getJSONObject("data")
                        .getLong("chatId");

            } else {

                throw new WebsiteHandlerException("Could not get the chatId");

            }

        } catch (Exception ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static ArrayList<ChatMessage> getChatMessages(long profileId,
                                                         String checksum) throws WebsiteHandlerException {

        try {

            // Let's do this!
            RequestHandler wh = new RequestHandler();
            ArrayList<ChatMessage> messageArray = new ArrayList<ChatMessage>();
            String httpContent;

            // Get the content
            httpContent = wh.post(

                    Constants.URL_CHAT_CONTENTS.replace(

                            "{UID}", profileId + ""), new PostData[]{

                    new PostData(

                            Constants.FIELD_NAMES_CHECKSUM[0], checksum

                    )

            }, 0

            );

            // Did we manage?
            if (!"".equals(httpContent)) {

                // Get the messages
                JSONArray messages = new JSONObject(httpContent)
                        .getJSONObject("data").getJSONObject("chat")
                        .getJSONArray("messages");
                JSONObject tempObject;

                // Iterate
                for (int i = 0, max = messages.length(); i < max; i++) {

                    tempObject = messages.optJSONObject(i);
                    messageArray.add(

                            new ChatMessage(

                                    profileId, tempObject.getLong("timestamp"), tempObject
                                    .getString("fromUsername"), tempObject
                                    .getString("message")

                            )

                    );

                }
                return messageArray;

            } else {

                throw new WebsiteHandlerException(
                        "Could not get the chat messages.");

            }

        } catch (Exception ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static boolean sendChatMessages(long chatId, String checksum,
                                           String message) throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler wh = new RequestHandler();
            String httpContent;

            // Get the content
            httpContent = wh.post(

                    Constants.URL_CHAT_SEND, new PostData[]{
                    new PostData(

                            Constants.FIELD_NAMES_CHAT[2], checksum

                    ), new PostData(

                    Constants.FIELD_NAMES_CHAT[1], chatId

            ), new PostData(

                    Constants.FIELD_NAMES_CHAT[0], message

            )

            }, 1

            );

            // Did we manage?
            if (!"".equals(httpContent)) {

                return true;

            } else {

                throw new WebsiteHandlerException(
                        "Could not send the chat message.");

            }

        } catch (RequestHandlerException ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static boolean closeChatWindow(long chatId)
            throws WebsiteHandlerException {

        try {

            // Let's login everybody!
            RequestHandler rh = new RequestHandler();

            // Get the content
            rh.get(

                    Constants.URL_CHAT_CLOSE.replace(

                            "{CID}", chatId + ""

                    ), 1

            );

            // Did we manage?
            return true;

        } catch (RequestHandlerException ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        }

    }

    public static boolean setActive() throws WebsiteHandlerException {

        try {

            // Let's see
            String httpContent = new RequestHandler().get(
                    Constants.URL_CHAT_SETACTIVE, 1);
            JSONObject httpResponse = new JSONObject(httpContent);

            // Is it ok?
            if (httpResponse.optString("message", "FAIL").equals("OK")) {

                return true;

            } else {

                return false;

            }

        } catch (RequestHandlerException ex) {

            throw new WebsiteHandlerException(ex.getMessage());

        } catch (Exception ex) {

            ex.printStackTrace();
            return false;

        }

    }
}