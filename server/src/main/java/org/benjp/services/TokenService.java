/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.benjp.services;

import com.mongodb.*;
import org.benjp.utils.MessageDigester;
import org.benjp.utils.PropertyManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named("tokenService")
@ApplicationScoped
public class TokenService
{
  private static final String M_TOKENS = "tokens";
  public static final String ANONIM_USER = "__anonim_";

  private DB db()
  {
    return MongoBootstrap.getDB();
  }

  public String getToken(String user)
  {
    String passphrase = PropertyManager.getProperty(PropertyManager.PROPERTY_PASSPHRASE);
    String in = user+passphrase;
    String token = MessageDigester.getHash(in);
    System.out.println("getToken :: user="+user+" ; token="+token);
    return token;
  }

  public boolean hasUserWithToken(String user, String token)
  {
    DBCollection coll = db().getCollection(M_TOKENS);
    BasicDBObject query = new BasicDBObject();
    query.put("user", user);
    query.put("token", token);
    DBCursor cursor = coll.find(query);
    return (cursor.hasNext());
  }

  public void addUser(String user, String token)
  {
    if (!hasUserWithToken(user, token))
    {
      System.out.println("TOKEN SERVICE :: ADDING :: " + user + " : " + token);
      removeUser(user);
      DBCollection coll = db().getCollection(M_TOKENS);

      BasicDBObject doc = new BasicDBObject();
      doc.put("_id", token);
      doc.put("user", user);
      doc.put("token", token);
      doc.put("validity", System.currentTimeMillis());
      doc.put("isDemoUser", user.startsWith(ANONIM_USER));

      coll.insert(doc);
    }
  }

  private void removeUser(String user)
  {
    DBCollection coll = db().getCollection(M_TOKENS);
    BasicDBObject query = new BasicDBObject();
    query.put("user", user);
    DBCursor cursor = coll.find(query);
    while (cursor.hasNext())
    {
      DBObject doc = cursor.next();
      coll.remove(doc);
    }
  }

  public void updateValidity(String user, String token)
  {
    DBCollection coll = db().getCollection(M_TOKENS);
    BasicDBObject query = new BasicDBObject();
    query.put("user", user);
    query.put("token", token);
    DBCursor cursor = coll.find(query);
    while (cursor.hasNext())
    {
      DBObject doc = cursor.next();
      doc.put("validity", System.currentTimeMillis());
      coll.save(doc, WriteConcern.SAFE);
    }
  }

  public List<String> getActiveUsersFilterBy(String user)
  {
    ArrayList<String> users = new ArrayList<String>();
    DBCollection coll = db().getCollection(M_TOKENS);
    BasicDBObject query = new BasicDBObject();
    query.put("isDemoUser", user.startsWith(ANONIM_USER));
    query.put("validity", new BasicDBObject("$gt", System.currentTimeMillis()-10*1000)); //check token not updated since 10sec
    DBCursor cursor = coll.find(query);
    while (cursor.hasNext())
    {
      DBObject doc = cursor.next();
      String target = doc.get("user").toString();
      if (!user.equals(target))
        users.add(target);
    }

    return users;
  }


}
