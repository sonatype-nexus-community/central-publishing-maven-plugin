/*
 * Copyright (c) 2022-present Sonatype, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.central.publisher;

import java.lang.reflect.Field;

public abstract class AbstractPlexusInjectionTest
{
  /**
   * Find all @Mock annotations in `this` (the child test class) and inject them into the corresponding fields in the
   * target
   *
   * @param target the object to inject mocks into
   */
  public void injectMocks(final Object target) throws IllegalAccessException {
    Field[] fields = getClass().getDeclaredFields();
    for (Field field : fields) {
      if (field.isAnnotationPresent(org.mockito.Mock.class)) {
        // Get the value for the field
        field.setAccessible(true);
        Object value = field.get(this);
        injectMockDependency(target, field.getName(), value);
      }
    }
  }

  public void injectValue(final Object target, final String fieldName, final Object value)
      throws IllegalAccessException, NoSuchFieldException
  {
    Field targetField = null;
    Class<?> targetClass = target.getClass();
    while (targetClass != null) {
      try {
        targetField = targetClass.getDeclaredField(fieldName);
        break;
      }
      catch (NoSuchFieldException e) {
        targetClass = targetClass.getSuperclass();
      }
    }
    if (targetField == null) {
      throw new NoSuchFieldException("Target field not found: " + fieldName);
    }

    targetField.setAccessible(true);
    targetField.set(target, value);
  }

  private void injectMockDependency(Object targetObject, String fieldName, Object mockInstance) {
    try {
      Field targetField = null;
      Class<?> targetClass = targetObject.getClass();
      while (targetClass != null) {
        try {
          targetField = targetClass.getDeclaredField(fieldName);
          break;
        }
        catch (NoSuchFieldException e) {
          targetClass = targetClass.getSuperclass();
        }
      }
      if (targetField == null) {
        throw new NoSuchFieldException("Target field not found: " + fieldName);
      }
      targetField.setAccessible(true);
      targetField.set(targetObject, mockInstance);
    }
    catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to inject mock dependency: " + fieldName, e);
    }
  }
}
