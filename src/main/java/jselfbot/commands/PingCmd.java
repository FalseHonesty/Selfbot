/*
 * Copyright 2016 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jselfbot.commands;

import java.time.temporal.ChronoUnit;

import jselfbot.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * @author John Grosh (jagrosh)
 */
public class PingCmd extends Command {

    public PingCmd() {
        this.name = "ping";
        this.description = "checks bot's connection";
        this.type = Type.KEEP_AND_RESEND;
    }

    @Override
    protected void execute(String args, MessageReceivedEvent event) {
        event.getMessage().editMessage("Pinging...")
                .queue(m -> m.editMessage("Ping: " + m.getCreationTime().until(m.getEditedTime(), ChronoUnit.MILLIS) + "ms")
                        .queue());
    }

}
