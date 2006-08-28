/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.webflow.execution.repository.continuation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.springframework.util.FileCopyUtils;
import org.springframework.webflow.execution.FlowExecution;

/**
 * A continuation implementation that is based on standard java serialization,
 * created by a {@link SerializedFlowExecutionContinuationFactory}.
 * 
 * @see SerializedFlowExecutionContinuationFactory
 * 
 * @author Keith Donald
 */
public class SerializedFlowExecutionContinuation extends FlowExecutionContinuation {

	/**
	 * The serialized flow execution.
	 */
	private byte[] data;

	/**
	 * Whether or not this flow execution array is compressed.
	 */
	private boolean compressed;

	/**
	 * Creates a new serialized flow execution continuation.
	 * @param flowExecution the flow execution
	 * @param compress whether or not the execution should be compressed
	 */
	public SerializedFlowExecutionContinuation(FlowExecution flowExecution, boolean compress) throws ContinuationCreationException{
        byte [] data;
        try {
            data = serialize(flowExecution);
            if (compress) {
                data = compress(data);
            }
        }
        catch (NotSerializableException e) {
            throw new ContinuationCreationException(flowExecution,
                    "Could not serialize flow execution; make sure all objects stored in flow scope are serializable",
                    e);
        }
        catch (IOException e) {
            throw new ContinuationCreationException(flowExecution,
                    "IOException thrown serializing flow execution -- this should not happen!", e);
        }
		this.data = data;
		this.compressed = compress;
	}

	/**
	 * Returns whether or not this byte array is compressed.
	 */
	public boolean isCompressed() {
		return compressed;
	}

	public FlowExecution unmarshal() throws ContinuationUnmarshalException {
		try {
            return deserialize(getData(true));
		}
		catch (IOException e) {
			throw new ContinuationUnmarshalException(
					"IOException thrown deserializing the flow execution stored in this continuation -- this should not happen!",
					e);
		}
		catch (ClassNotFoundException e) {
			throw new ContinuationUnmarshalException(
					"ClassNotFoundException thrown deserializing the flow execution stored in this continuation -- "
							+ "This should not happen! Make sure there are no classloader issues."
							+ "For example, perhaps the Web Flow system is being loaded by a classloader "
							+ "that is a parent of the classloader loading application classes?", e);
		}
	}

	public byte[] toByteArray() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length + 128);
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			try {
				oos.writeObject(this);
				oos.flush();
			}
			finally {
				oos.close();
			}
			return baos.toByteArray();
		}
		catch (IOException e) {
			throw new IllegalStateException();
		}
	}

	/**
	 * Return the flow execution in its raw byte[] form. Will decompress if
	 * requested.
	 * @param decompress whether or not to decompress the byte[] array before
	 * returning
	 * @return the byte array
	 * @throws IOException a problem occured with decompression
	 */
	public byte[] getData(boolean decompress) throws IOException {
		if (isCompressed() && decompress) {
			return decompress(data);
		}
		else {
			return data;
		}
	}

    /**
     * Internal helper method to serialize data. Override if a custom
     * serialization method is used.
     * 
     * @param flowExecution flow to serialize
     * @return serialized flow data.
     * @throws IOException passed through from stream.
     */
    protected byte [] serialize(FlowExecution flowExecution) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(384);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        try {
            oos.writeObject(flowExecution);
            oos.flush();
            return baos.toByteArray();
        }
        finally {
            oos.close();
        }
    }
    
    /**
     * Internal helper method to deserialize data. Override if a custom
     * serialization method is used.
     * 
     * @param data serialized flow data.
     * @return Deserialized flow
     * @throws IOException passed through from stream.
     * @throws ClassNotFoundException passed from stream.
     */
    protected FlowExecution deserialize(byte [] data) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        try {
            return (FlowExecution)ois.readObject();
        }
        finally {
            ois.close();
        }
    }
    
    /**
     * Internal helper method to compress given data using GZIP compression.
     */
    protected byte[] compress(byte[] dataToCompress) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzipos = new GZIPOutputStream(baos);
        try {
            gzipos.write(dataToCompress);
            gzipos.flush();
        }
        finally {
            gzipos.close();
        }
        return baos.toByteArray();
    }
    
	/**
	 * Internal helper method to decompress given data using GZIP decompression.
	 */
    protected byte[] decompress(byte[] dataToDecompress) throws IOException {
		GZIPInputStream gzipin = new GZIPInputStream(new ByteArrayInputStream(dataToDecompress));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			FileCopyUtils.copy(gzipin, baos);
		}
		finally {
			gzipin.close();
		}
		return baos.toByteArray();
	}
}