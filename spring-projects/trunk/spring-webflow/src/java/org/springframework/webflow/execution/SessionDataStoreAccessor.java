package org.springframework.webflow.execution;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.webflow.Event;

/**
 * A data source accessor that accesses the external context session map.
 * @author Keith Donald
 */
public class SessionDataStoreAccessor implements DataStoreAccessor {
	public MutableAttributeSource getDataStore(Event sourceEvent) {
		if (sourceEvent instanceof ExternalEvent) {
			return new MapAttributeSource(((ExternalEvent)sourceEvent).getSessionMap());
		}
		else {
			throw new IllegalStateException("This session data store accessor was invoked; however, " + "the source event '"
					+ sourceEvent.getId() + "' signaled is not an instance of ExternalEvent: "
					+ "there no way to access the 'sessionMap' property: programmer error");
		}
	}
}