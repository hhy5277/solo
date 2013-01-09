/*
 * Copyright (c) 2009, 2010, 2011, 2012, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.solo.event.plugin;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.plugin.AbstractPlugin;
import org.b3log.latke.plugin.PluginManager;
import org.b3log.latke.repository.Transaction;
import org.b3log.solo.repository.PluginRepository;
import org.b3log.solo.repository.impl.PluginRepositoryImpl;
import org.b3log.solo.util.Plugins;


/**
 * This listener is responsible for refreshing plugin after every loaded.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Nov 28, 2011
 * @since 0.3.1
 */
public final class PluginRefresher extends AbstractEventListener<List<AbstractPlugin>> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PluginRefresher.class.getName());

    /**
     * Plugin repository.
     */
    private PluginRepository pluginRepository = PluginRepositoryImpl.getInstance();

    @Override
    public void action(final Event<List<AbstractPlugin>> event) throws
            EventException {
        final List<AbstractPlugin> plugins = event.getData();

        LOGGER.log(Level.FINER, "Processing an event[type={0}, data={1}] in listener[className={2}]",
            new Object[] {event.getType(), plugins, PluginRefresher.class.getName()});

        final Transaction transaction = pluginRepository.beginTransaction();

        transaction.clearQueryCache(false);
        try {
            Plugins.refresh(plugins);
            transaction.commit();
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.SEVERE, "Processing plugin loaded event error", e);
            throw new EventException(e);
        }
    }

    /**
     * Gets the event type {@linkplain PluginManager#PLUGIN_LOADED_EVENT}.
     * 
     * @return event type
     */
    @Override
    public String getEventType() {
        return PluginManager.PLUGIN_LOADED_EVENT;
    }
}
