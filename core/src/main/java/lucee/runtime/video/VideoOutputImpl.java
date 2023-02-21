/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.video;

import lucee.commons.io.res.Resource;

public class VideoOutputImpl implements VideoOutput {

	private Resource resource;
	private double offset = 0;
	private String comment;
	private String title;
	private String author;
	private String copyright;
	private int fileLimitation;
	private long maxFrames = 0;
	private String format;
	private int frameRate;

	public VideoOutputImpl(Resource resource) {
		this.resource = resource;
	}

	/**
	 * set time offset of the output file based on input file in seconds
	 * 
	 * @param offset
	 */
	@Override
	public void setOffset(double offset) {
		this.offset = offset;
	}

	/**
	 * sets a comment to the output video
	 * 
	 * @param comment
	 */
	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * sets a title to the output video
	 * 
	 * @param title
	 */
	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * sets an author to the output video
	 * 
	 * @param author
	 */
	@Override
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * sets a copyright to the output video
	 * 
	 * @param copyright
	 */
	@Override
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	/**
	 * @return the res
	 */
	@Override
	public Resource getResource() {
		return resource;
	}

	/**
	 * @return the offset
	 */
	@Override
	public double getOffset() {
		return offset;
	}

	/**
	 * @return the comment
	 */
	@Override
	public String getComment() {
		return comment;
	}

	/**
	 * @return the title
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * @return the author
	 */
	@Override
	public String getAuthor() {
		return author;
	}

	/**
	 * @return the copyright
	 */
	@Override
	public String getCopyright() {
		return copyright;
	}

	/**
	 * @return the fileLimitation
	 */
	@Override
	public int getFileLimitation() {
		return fileLimitation;
	}

	/**
	 * limit size of the output file
	 * 
	 * @param size the size to set
	 */
	@Override
	public void limitFileSizeTo(int size) {
		this.fileLimitation = size;
	}

	/**
	 * @return the maxFrames
	 */
	@Override
	public long getMaxFrames() {
		return maxFrames;
	}

	/**
	 * @param maxFrames the maxFrames to set
	 */
	@Override
	public void setMaxFrames(long maxFrames) {
		this.maxFrames = maxFrames;
	}

	/**
	 * @param resource the resource to set
	 */
	@Override
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * @return the format
	 */
	@Override
	public String getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	@Override
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * @param fileLimitation the fileLimitation to set
	 */
	@Override
	public void setFileLimitation(int fileLimitation) {
		this.fileLimitation = fileLimitation;
	}

	/**
	 * @see lucee.runtime.video.VideoOutput#getFrameRate()
	 */
	@Override
	public int getFrameRate() {
		return frameRate;
	}

	/**
	 * @see lucee.runtime.video.VideoOutput#setFrameRate(int)
	 */
	@Override
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}
}