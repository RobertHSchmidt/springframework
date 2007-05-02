/*
 * Copyright 2004-2007 the original author or authors.
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
package org.springframework.aws.maven;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.resource.Resource;

public class TransferListenerSupport {

	private Wagon wagon;

	private Set<TransferListener> listeners = new HashSet<TransferListener>();

	public TransferListenerSupport(Wagon wagon) {
		this.wagon = wagon;
	}

	public void addListener(TransferListener listener) {
		listeners.add(listener);
	}

	public void removeListener(TransferListener listener) {
		listeners.remove(listener);
	}

	public boolean hasListener(TransferListener listener) {
		return listeners.contains(listener);
	}

	public void fireTransferInitiated(Resource resource, int requestType) {
		TransferEvent event = new TransferEvent(wagon, resource,
				TransferEvent.TRANSFER_INITIATED, requestType);
		for (TransferListener listener : listeners) {
			listener.transferInitiated(event);
		}
	}

	public void fireTransferStarted(Resource resource, int requestType) {
		TransferEvent event = new TransferEvent(wagon, resource,
				TransferEvent.TRANSFER_STARTED, requestType);
		for (TransferListener listener : listeners) {
			listener.transferStarted(event);
		}
	}

	public void fireTransferCompleted(Resource resource, int requestType) {
		TransferEvent event = new TransferEvent(wagon, resource,
				TransferEvent.TRANSFER_COMPLETED, requestType);
		for (TransferListener listener : listeners) {
			listener.transferCompleted(event);
		}
	}

	public void fireTransferError(Resource resource, int requestType,
			Exception e) {
		TransferEvent event = new TransferEvent(wagon, resource, e, requestType);
		for (TransferListener listener : listeners) {
			listener.transferError(event);
		}
	}
}
