/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.cos;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.Vector;

import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * This will take an array of streams and sequence them together.
 *
 * @author Ben Litchfield
 */
public class COSStreamArray extends COSStream
{
    private COSArray streams;

    /**
     * The first stream will be used to delegate some of the methods for this
     * class.
     */
    private COSStream firstStream;

    /**
     * Constructor.
     *
     * @param array The array of COSStreams to concatenate together.
     */
    public COSStreamArray( COSArray array )
    {
        super( new COSDictionary() );
        streams = array;
        if( array.size() > 0 )
        {
            firstStream = (COSStream)array.getObject( 0 );
        }
    }

    /**
     * This will get a stream (or the reference to a stream) from the array.
     *
     * @param index The index of the requested stream
     * @return The stream object or a reference to a stream
     */
    public COSBase get( int index )
    {
        return streams.get( index );
    }

    /**
     * This will get the number of streams in the array.
     *
     * @return the number of streams
     */
    public int getStreamCount()
    {
        return streams.size();
    }

    /**
     * This will get an object from this streams dictionary.
     *
     * @param key The key to the object.
     *
     * @return The dictionary object with the key or null if one does not exist.
     */
    @Override
    public COSBase getItem( COSName key )
    {
        return firstStream.getItem( key );
    }

   /**
     * This will get an object from this streams dictionary and dereference it
     * if necessary.
     *
     * @param key The key to the object.
     *
     * @return The dictionary object with the key or null if one does not exist.
     */
    @Override
    public COSBase getDictionaryObject( COSName key )
    {
        return firstStream.getDictionaryObject( key );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "COSStream{}";
    }

    /**
     * This will get the dictionary that is associated with this stream.
     *
     * @return the object that is associated with this stream.
     */
    public COSDictionary getDictionary()
    {
        return firstStream;
    }

    /**
     * This will get the stream with all of the filters applied.
     *
     * @return the bytes of the physical (endoced) stream
     *
     * @throws IOException when encoding/decoding causes an exception
     */
    @Override
    public InputStream getFilteredStream() throws IOException
    {
        throw new IOException( "Error: Not allowed to get filtered stream from array of streams." );
    }

    /**
     * This will get the logical content stream with none of the filters.
     *
     * @return the bytes of the logical (decoded) stream
     *
     * @throws IOException when encoding/decoding causes an exception
     */
    @Override
    public InputStream getUnfilteredStream() throws IOException
    {
        Vector<InputStream> inputStreams = new Vector<InputStream>();
        byte[] inbetweenStreamBytes = "\n".getBytes("ISO-8859-1");

        for( int i=0;i<streams.size(); i++ )
        {
            COSStream stream = (COSStream)streams.getObject( i );
            inputStreams.add( stream.getUnfilteredStream() );
            //handle the case where there is no whitespace in the
            //between streams in the contents array, without this
            //it is possible that two operators will get concatenated
            //together
            inputStreams.add( new ByteArrayInputStream( inbetweenStreamBytes ) );
        }

        return new SequenceInputStream( inputStreams.elements() );
    }

    @Override
    public void accept(COSVisitor visitor) throws IOException
    {
        visitor.visit(this);
    }

    /**
     * This will return the filters to apply to the byte stream
     * the method will return.
     * - null if no filters are to be applied
     * - a COSName if one filter is to be applied
     * - a COSArray containing COSNames if multiple filters are to be applied
     *
     * @return the COSBase object representing the filters
     */
    @Override
    public COSBase getFilters()
    {
        return firstStream.getFilters();
    }

    /**
     * This will create a new stream for which filtered byte should be
     * written to.  You probably don't want this but want to use the
     * createUnfilteredStream, which is used to write raw bytes to.
     *
     * @return A stream that can be written to.
     *
     * @throws IOException If there is an error creating the stream.
     */
    @Override
    public OutputStream createFilteredStream() throws IOException
    {
        return firstStream.createFilteredStream();
    }

    /**
     * set the filters to be applied to the stream.
     *
     * @param filters The filters to set on this stream.
     *
     * @throws IOException If there is an error clearing the old filters.
     */
    @Override
    public void setFilters(COSBase filters) throws IOException
    {
        //should this be allowed?  Should this
        //propagate to all streams in the array?
        firstStream.setFilters( filters );
    }

    /**
     * This will create an output stream that can be written to.
     *
     * @return An output stream which raw data bytes should be written to.
     *
     * @throws IOException If there is an error creating the stream.
     */
    @Override
    public OutputStream createUnfilteredStream() throws IOException
    {
        return firstStream.createUnfilteredStream();
    }

    /**
     * Appends a new stream to the array that represents this object's stream.
     *
     * @param streamToAppend The stream to append.
     */
    public void appendStream(COSStream streamToAppend)
    {
        streams.add(streamToAppend);
    }
    
    /**
     * Insert the given stream at the beginning of the existing stream array.
     * @param streamToBeInserted
     */
    public void insertCOSStream(PDStream streamToBeInserted)
    {
        COSArray tmp = new COSArray();
        tmp.add(streamToBeInserted);
        tmp.addAll(streams);
        streams.clear();
        streams = tmp;
    }

}
