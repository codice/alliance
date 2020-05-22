/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.video.stream.mpegts;

import ddf.security.Subject;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.PrincipalCollection;

/** Trivial implementation that skips any security checks. */
public class SimpleSubject implements Subject {

  @Override
  public Object getPrincipal() {
    return null;
  }

  @Override
  public PrincipalCollection getPrincipals() {
    return null;
  }

  @Override
  public boolean isPermitted(String permission) {
    return false;
  }

  @Override
  public boolean isPermitted(Permission permission) {
    return false;
  }

  @Override
  public boolean[] isPermitted(String... permissions) {
    return new boolean[0];
  }

  @Override
  public boolean[] isPermitted(List<Permission> permissions) {
    return new boolean[0];
  }

  @Override
  public boolean isPermittedAll(String... permissions) {
    return false;
  }

  @Override
  public boolean isPermittedAll(Collection<Permission> permissions) {
    return false;
  }

  @Override
  public void checkPermission(String permission) throws AuthorizationException {}

  @Override
  public void checkPermission(Permission permission) throws AuthorizationException {}

  @Override
  public void checkPermissions(String... permissions) throws AuthorizationException {}

  @Override
  public void checkPermissions(Collection<Permission> permissions) throws AuthorizationException {}

  @Override
  public boolean hasRole(String roleIdentifier) {
    return false;
  }

  @Override
  public boolean[] hasRoles(List<String> roleIdentifiers) {
    return new boolean[0];
  }

  @Override
  public boolean hasAllRoles(Collection<String> roleIdentifiers) {
    return false;
  }

  @Override
  public void checkRole(String roleIdentifier) throws AuthorizationException {}

  @Override
  public void checkRoles(Collection<String> roleIdentifiers) throws AuthorizationException {}

  @Override
  public void checkRoles(String... roleIdentifiers) throws AuthorizationException {}

  @Override
  public void login(AuthenticationToken token) throws AuthenticationException {}

  @Override
  public boolean isAuthenticated() {
    return false;
  }

  @Override
  public boolean isRemembered() {
    return false;
  }

  @Override
  public Session getSession() {
    return null;
  }

  @Override
  public Session getSession(boolean create) {
    return null;
  }

  @Override
  public void logout() {}

  @Override
  public <V> V execute(Callable<V> callable) throws ExecutionException {
    try {
      return callable.call();
    } catch (Exception e) {
      throw new ExecutionException(e);
    }
  }

  @Override
  public void execute(Runnable runnable) {}

  @Override
  public <V> Callable<V> associateWith(Callable<V> callable) {
    return null;
  }

  @Override
  public Runnable associateWith(Runnable runnable) {
    return null;
  }

  @Override
  public void runAs(PrincipalCollection principals)
      throws NullPointerException, IllegalStateException {}

  @Override
  public boolean isRunAs() {
    return false;
  }

  @Override
  public PrincipalCollection getPreviousPrincipals() {
    return null;
  }

  @Override
  public PrincipalCollection releaseRunAs() {
    return null;
  }

  @Override
  public String getName() {
    return getClass().getName();
  }
}
