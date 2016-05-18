/**
 * Copyright (c) Codice Foundation
 * <p>
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. A copy of the GNU Lesser General Public License
 * is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.alliance.nsili.endpoint;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

import ddf.security.Subject;
import ddf.security.service.SecurityManager;
import ddf.security.service.SecurityServiceException;

public class TestNsiliCommon {

    protected SecurityManager securityManager = mock(SecurityManager.class);

    protected Subject mockSubject;

    protected void setupCommonMocks() throws SecurityServiceException {
        mockSubject = new Subject() {
            @Override
            public boolean isGuest() {
                return true;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public PrincipalCollection getPrincipals() {
                return null;
            }

            @Override
            public boolean isPermitted(String s) {
                return false;
            }

            @Override
            public boolean isPermitted(Permission permission) {
                return false;
            }

            @Override
            public boolean[] isPermitted(String... strings) {
                return new boolean[0];
            }

            @Override
            public boolean[] isPermitted(List<Permission> list) {
                return new boolean[0];
            }

            @Override
            public boolean isPermittedAll(String... strings) {
                return false;
            }

            @Override
            public boolean isPermittedAll(Collection<Permission> collection) {
                return false;
            }

            @Override
            public void checkPermission(String s) throws AuthorizationException {

            }

            @Override
            public void checkPermission(Permission permission) throws AuthorizationException {

            }

            @Override
            public void checkPermissions(String... strings) throws AuthorizationException {

            }

            @Override
            public void checkPermissions(Collection<Permission> collection)
                    throws AuthorizationException {

            }

            @Override
            public boolean hasRole(String s) {
                return false;
            }

            @Override
            public boolean[] hasRoles(List<String> list) {
                return new boolean[0];
            }

            @Override
            public boolean hasAllRoles(Collection<String> collection) {
                return false;
            }

            @Override
            public void checkRole(String s) throws AuthorizationException {

            }

            @Override
            public void checkRoles(Collection<String> collection) throws AuthorizationException {

            }

            @Override
            public void checkRoles(String... strings) throws AuthorizationException {

            }

            @Override
            public void login(AuthenticationToken authenticationToken)
                    throws AuthenticationException {

            }

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
            public Session getSession(boolean b) {
                return null;
            }

            @Override
            public void logout() {

            }

            @Override
            public <V> V execute(Callable<V> callable) throws ExecutionException {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new ExecutionException(e);
                }
            }

            @Override
            public void execute(Runnable runnable) {
                runnable.run();
            }

            @Override
            public <V> Callable<V> associateWith(Callable<V> callable) {
                return null;
            }

            @Override
            public Runnable associateWith(Runnable runnable) {
                return null;
            }

            @Override
            public void runAs(PrincipalCollection principalCollection)
                    throws NullPointerException, IllegalStateException {

            }

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
        };

        when(securityManager.getSubject(any(Object.class))).thenReturn(mockSubject);
    }
}
