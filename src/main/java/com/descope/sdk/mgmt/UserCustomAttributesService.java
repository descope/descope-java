package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.customattributes.CreateCustomAttributesRequest;
import com.descope.model.customattributes.CustomAttributesResponse;
import com.descope.model.customattributes.DeleteCustomAttributesRequest;

/** Provides audit records search capabilities. */
public interface UserCustomAttributesService {

  /**
   * Get all user custom attributes
   *
   * @return {@link CustomAttributesResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  CustomAttributesResponse getCustomAttributes() throws DescopeException;

  /**
   * Create custom attributes.
   *
   * @param request the custom attribtues (1 or more) to create
   * @return {@link CustomAttributesResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  CustomAttributesResponse createCustomAttributes(CreateCustomAttributesRequest request) throws DescopeException;

  /**
   * Delete custom attributes.
   *
   * @param request the names of the attributes to delete
   * @return {@link CustomAttributesResponse}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  CustomAttributesResponse deleteCustomAttributes(DeleteCustomAttributesRequest request) throws DescopeException;
}
