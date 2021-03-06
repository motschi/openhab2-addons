/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;

import com.google.gson.JsonObject;

/**
 * @author Patrick Boos - Initial contribution
 * @author Dimalo
 */
public class XiaomiActorPlugHandler extends XiaomiActorBaseHandler {

    public XiaomiActorPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    void execute(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_POWER_ON)) {
            String status = command.toString().toLowerCase();
            getXiaomiBridgeHandler().writeToDevice(itemId, new String[] { "status" }, new Object[] { status });
        } else {
            logger.error("Can't handle command {} on channel {}", command, channelUID);
        }
    }

    @Override
    void parseHeartbeat(JsonObject data) {
        getStatusFromData(data);
        if (data.has("inuse")) {
            updateState(CHANNEL_IN_USE, (data.get("inuse").getAsInt() == 1) ? OnOffType.ON : OnOffType.OFF);
        }
        if (data.has("load_power")) {
            updateState(CHANNEL_LOAD_POWER, new DecimalType(data.get("load_power").getAsBigDecimal()));
        }
        if (data.has("power_consumed")) {
            updateState(CHANNEL_POWER_CONSUMED,
                    new DecimalType(data.get("power_consumed").getAsBigDecimal().scaleByPowerOfTen(-3)));
        }
    }

    /**
     * @param data
     */
    private void getStatusFromData(JsonObject data) {
        if (data.has("status")) {
            boolean isOn = data.get("status").getAsString().equals("on");
            updateState(CHANNEL_POWER_ON, isOn ? OnOffType.ON : OnOffType.OFF);
            if (!isOn) {
                updateState(CHANNEL_IN_USE, OnOffType.OFF);
                updateState(CHANNEL_LOAD_POWER, new DecimalType(0));
            }
        }
    }

    @Override
    void parseReadAck(JsonObject data) {
        parseHeartbeat(data);
    }

    @Override
    void parseReport(JsonObject data) {
        getStatusFromData(data);
    }

    @Override
    void parseWriteAck(JsonObject data) {
        parseHeartbeat(data);
    }
}
