/*  Copyright (C) 1998-2002 Regents of the University of California *  This file is part of ManyMinds. * *  ManyMinds is free software; you can redistribute it and/or modify *  it under the terms of the GNU General Public License as published by *  the Free Software Foundation; either version 2 of the License, or *  (at your option) any later version. *   *  ManyMinds is distributed in the hope that it will be useful, *  but WITHOUT ANY WARRANTY; without even the implied warranty of *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the *  GNU General Public License for more details. *   *  You should have received a copy of the GNU General Public License *  along with ManyMinds; if not, write to the Free Software *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */ package manyminds.webserver;/* * HttpConstants.java * * Written by Eric Eslinger * Copyright � 1998-1999 University of California * All Rights Reserved. * * Agenda *	Add self-documenting javadoc comments to the methods. * * History *	16 JUN 99 EME commented code. * *//** *	Defines a bunch of codes used by HTTP. *	@author Eric M Eslinger */interface HttpConstants {	/** 2XX: generally "OK" */	public static final int HTTP_OK = 200;	public static final int HTTP_CREATED = 201;	public static final int HTTP_ACCEPTED = 202;	public static final int HTTP_NOT_AUTHORITATIVE = 203;	public static final int HTTP_NO_CONTENT = 204;	public static final int HTTP_RESET = 205;	public static final int HTTP_PARTIAL = 206;	/** 3XX: relocation/redirect */	public static final int HTTP_MULT_CHOICE = 300;	public static final int HTTP_MOVED_PERM = 301;	public static final int HTTP_MOVED_TEMP = 302;	public static final int HTTP_SEE_OTHER = 303;	public static final int HTTP_NOT_MODIFIED = 304;	public static final int HTTP_USE_PROXY = 305;	/** 4XX: client error */	public static final int HTTP_BAD_REQUEST = 400;	public static final int HTTP_UNAUTHORIZED = 401;	public static final int HTTP_PAYMENT_REQUIRED = 402;	public static final int HTTP_FORBIDDEN = 403;	public static final int HTTP_NOT_FOUND = 404;	public static final int HTTP_BAD_METHOD = 405;	public static final int HTTP_NOT_ACCEPTABLE = 406;	public static final int HTTP_PROXY_AUTH = 407;	public static final int HTTP_CLIENT_TIMEOUT = 408;	public static final int HTTP_CONFLICT = 409;	public static final int HTTP_GONE = 410;	public static final int HTTP_LENGTH_REQUIRED = 411;	public static final int HTTP_PRECON_FAILED = 412;	public static final int HTTP_ENTITY_TOO_LARGE = 413;	public static final int HTTP_REQ_TOO_LONG = 414;	public static final int HTTP_UNSUPPORTED_TYPE = 415;	/** 5XX: server error */	public static final int HTTP_SERVER_ERROR = 500;	public static final int HTTP_INTERNAL_ERROR = 501;	public static final int HTTP_BAD_GATEWAY = 502;	public static final int HTTP_UNAVAILABLE = 503;	public static final int HTTP_GATEWAY_TIMEOUT = 504;	public static final int HTTP_VERSION = 505;}