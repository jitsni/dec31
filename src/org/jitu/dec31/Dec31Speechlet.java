/**
 * Copyright 2017 Jitendra Kotamraju.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitu.dec31;

import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.OutputSpeech;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Dec31Speechlet implements SpeechletV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dec31Speechlet.class);

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        LOGGER.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        Session session = requestEnvelope.getSession();

        session.setAttribute("lastMonth", 1);
        session.setAttribute("lastDate", 1);
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Intent intent = request.getIntent();
        String intentName = intent.getName();
        Slot dateSlot = intent.getSlot("date");

        if ("DecIntent".equals(intentName) && dateSlot != null && dateSlot.getValue() != null) {
            String date = dateSlot.getValue();
            LOGGER.info("onIntent date={}", date);
            MonthDate monthDate = monthDate(date);
            return getGameResponse(requestEnvelope.getSession(), monthDate);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getWelcomeResponse();
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            return getStopResponse();
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            return getCancelResponse();
        } else {
            return getAskResponse("Ok", "This is unsupported.  Please try something else.");
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        LOGGER.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText =
                "The game starts on January 1st. " +
                "You and I take turns calling out another date with either the same month OR the same day. " +
                "Whoever calls out December 31 wins the game. Your turn";
        return getAskResponse("Help", speechText);
    }

    private SpeechletResponse getGameResponse(Session session, MonthDate monthDate) {
        int lastMonth = (Integer) session.getAttribute("lastMonth");
        int lastDate = (Integer) session.getAttribute("lastDate");

        if (monthDate == null) {
            // Invalid date
            String lastStr = Graph.print(lastMonth - 1, lastDate -1);
            String response = " Invalid date. Pick one after " + lastStr + ". Your turn again";
            return getAskResponse("Invalid", response);
        } else if ((monthDate.month  == lastMonth && monthDate.date > lastDate) ||
                (monthDate.date == lastDate && monthDate.month > lastMonth)) {

            // Valid pick
            if (monthDate.month == 12 && monthDate.date == 31) {
                return getTellResponse("You win !!");
            } else {
                MonthDate responseMonthDate = Graph.transition(monthDate);
                String response = Graph.print(responseMonthDate);
                if (responseMonthDate.month == 12 && responseMonthDate.date == 31) {
                    return getTellResponse(response + ". I win !!");
                } else {
                    session.setAttribute("lastMonth", responseMonthDate.month);
                    session.setAttribute("lastDate", responseMonthDate.date);

                    return getAskResponse("OK", response + ". Your turn");
                }
            }
        } else {
            // Invalid pick
            String monthDateStr = Graph.print(monthDate);
            String lastStr = Graph.print(lastMonth - 1, lastDate -1);
            String response = monthDateStr + " is invalid after " + lastStr + ". Your turn again";
            return getAskResponse("Invalid", response);
        }
    }

    private SpeechletResponse getStopResponse() {
        String speechText = "Goodbye";
        return getTellResponse(speechText);
    }

    private SpeechletResponse getCancelResponse() {
        String speechText = "Goodbye";
        return getTellResponse(speechText);
    }

    /**
     * Helper method that creates a card object.
     * @param title title of the card
     * @param content body of the card
     * @return SimpleCard the display card to be sent along with the voice response.
     */
    private SimpleCard getSimpleCard(String title, String content) {
        SimpleCard card = new SimpleCard();
        card.setTitle(title);
        card.setContent(content);

        return card;
    }

    /**
     * Helper method for retrieving an OutputSpeech object when given a string of TTS.
     * @param speechText the text that should be spoken out to the user.
     * @return an instance of SpeechOutput.
     */
    private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return speech;
    }

    /**
     * Helper method that returns a reprompt object. This is used in Ask responses where you want
     * the user to be able to respond to your speech.
     * @param outputSpeech The OutputSpeech object that will be said once and repeated if necessary.
     * @return Reprompt instance.
     */
    private Reprompt getReprompt(OutputSpeech outputSpeech) {
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return reprompt;
    }

    /**
     * Helper method for retrieving an Ask response with a simple card and reprompt included.
     * @param cardTitle Title of the card that you want displayed.
     * @param speechText speech text that will be spoken to the user.
     * @return the resulting card and speech text.
     */
    private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
        SimpleCard card = getSimpleCard(cardTitle, speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
        Reprompt reprompt = getReprompt(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    private SpeechletResponse getTellResponse(String speechText) {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(speechText);

        return SpeechletResponse.newTellResponse(outputSpeech);
    }

    private MonthDate monthDate(String str)
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-d");
        Date date;
        try {
            date = dateFormat.parse(str);
        } catch (ParseException e) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        return new MonthDate(month, day);
    }

}
