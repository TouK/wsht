/*
 * Copyright (c) 2009 TouK sp. z o.o. s.k.a.
 * All rights reserved
 */

package pl.touk.humantask;

import pl.touk.humantask.model.spec.TaskDefinition;

/**
 * Task retrieving operations. We assume that task name is unique
 * in whole configuration.
 *
 * @author <a href="mailto:jkr@touk.pl">Jakub Kurlenda</a>
 * @author Witek Wołejszo
 */
public interface HumanInteractionsManager {
    
    /*
     * Retrieves task definition by name.
     *
     * @param taskName
     * @return TaskDefinition instance
     * @throws HTConfigurationException in case when no such task definition was found
     */
    TaskDefinition getTaskDefinition(String taskName);

}