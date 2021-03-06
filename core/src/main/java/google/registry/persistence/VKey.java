// Copyright 2020 The Nomulus Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package google.registry.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static google.registry.util.PreconditionsUtils.checkArgumentNotNull;

import com.googlecode.objectify.Key;
import google.registry.model.BackupGroupRoot;
import google.registry.model.ImmutableObject;
import google.registry.model.translators.VKeyTranslatorFactory;
import java.io.Serializable;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * VKey is an abstraction that encapsulates the key concept.
 *
 * <p>A VKey instance must contain both the JPA primary key for the referenced entity class and the
 * objectify key for the object.
 */
public class VKey<T> extends ImmutableObject implements Serializable {

  private static final long serialVersionUID = -5291472863840231240L;

  // The primary key for the referenced entity.
  private final Object primaryKey;

  // The objectify key for the referenced entity.
  private final com.googlecode.objectify.Key<T> ofyKey;

  private final Class<? extends T> kind;

  private VKey(Class<? extends T> kind, com.googlecode.objectify.Key<T> ofyKey, Object primaryKey) {
    this.kind = kind;
    this.ofyKey = ofyKey;
    this.primaryKey = primaryKey;
  }

  /**
   * Creates a {@link VKey} which only contains the sql primary key.
   *
   * <p>Deprecated. Create symmetric keys with create() instead.
   */
  public static <T> VKey<T> createSql(Class<T> kind, Object sqlKey) {
    checkArgumentNotNull(kind, "kind must not be null");
    checkArgumentNotNull(sqlKey, "sqlKey must not be null");
    return new VKey<T>(kind, null, sqlKey);
  }

  /** Creates a {@link VKey} which only contains the ofy primary key. */
  public static <T> VKey<T> createOfy(Class<T> kind, com.googlecode.objectify.Key<T> ofyKey) {
    checkArgumentNotNull(kind, "kind must not be null");
    checkArgumentNotNull(ofyKey, "ofyKey must not be null");
    return new VKey<T>(kind, ofyKey, null);
  }

  /** Creates a {@link VKey} which only contains both sql and ofy primary key. */
  public static <T> VKey<T> create(
      Class<T> kind, Object sqlKey, com.googlecode.objectify.Key<T> ofyKey) {
    checkArgumentNotNull(kind, "kind must not be null");
    checkArgumentNotNull(sqlKey, "sqlKey must not be null");
    checkArgumentNotNull(ofyKey, "ofyKey must not be null");
    return new VKey<T>(kind, ofyKey, sqlKey);
  }

  /**
   * Creates a symmetric {@link VKey} in which both sql and ofy keys are {@code id}.
   *
   * <p>IMPORTANT USAGE NOTE: Datastore entities that are not roots of entity groups (i.e. those
   * that do not have a null parent in their Objectify keys) require the full entity group
   * inheritance chain to be specified and thus cannot use this create method. You need to use
   * {@link #create(Class, Object, Key)} instead and pass in the full, valid parent field in the
   * Datastore key.
   */
  public static <T> VKey<T> create(Class<T> kind, long id) {
    checkArgument(
        kind.isAssignableFrom(BackupGroupRoot.class),
        "The kind %s is not a BackupGroupRoot and thus needs its entire entity group chain"
            + " specified in a parent",
        kind.getCanonicalName());
    return new VKey<T>(kind, Key.create(kind, id), id);
  }

  /**
   * Creates a symmetric {@link VKey} in which both sql and ofy keys are {@code name}.
   *
   * <p>IMPORTANT USAGE NOTE: Datastore entities that are not roots of entity groups (i.e. those
   * that do not have a null parent in their Objectify keys) require the full entity group
   * inheritance chain to be specified and thus cannot use this create method. You need to use
   * {@link #create(Class, Object, Key)} instead and pass in the full, valid parent field in the
   * Datastore key.
   */
  public static <T> VKey<T> create(Class<T> kind, String name) {
    checkArgument(
        kind.isAssignableFrom(BackupGroupRoot.class),
        "The kind %s is not a BackupGroupRoot and thus needs its entire entity group chain"
            + " specified in a parent",
        kind.getCanonicalName());
    return new VKey<T>(kind, Key.create(kind, name), name);
  }

  /** Returns the type of the entity. */
  public Class<? extends T> getKind() {
    return this.kind;
  }

  /** Returns the SQL primary key. */
  public Object getSqlKey() {
    checkState(primaryKey != null, "Attempting obtain a null SQL key.");
    return this.primaryKey;
  }

  /** Returns the SQL primary key if it exists. */
  public Optional<Object> maybeGetSqlKey() {
    return Optional.ofNullable(this.primaryKey);
  }

  /** Returns the objectify key. */
  public com.googlecode.objectify.Key<T> getOfyKey() {
    checkState(ofyKey != null, "Attempting obtain a null Objectify key.");
    return this.ofyKey;
  }

  /** Returns the objectify key if it exists. */
  public Optional<com.googlecode.objectify.Key<T>> maybeGetOfyKey() {
    return Optional.ofNullable(this.ofyKey);
  }

  /** Convenience method to construct a VKey from an objectify Key. */
  @Nullable
  public static <T> VKey<T> from(Key<T> key) {
    return VKeyTranslatorFactory.createVKey(key);
  }
}
