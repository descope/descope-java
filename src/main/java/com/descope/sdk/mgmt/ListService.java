package com.descope.sdk.mgmt;

import com.descope.exception.DescopeException;
import com.descope.model.list.DescopeList;
import com.descope.model.list.ListRequest;
import java.util.List;

/** Provides functions for managing lists (texts, IPs or JSON) in a project. */
public interface ListService {

  /**
   * Create a new list.
   *
   * @param request the list to create. The name is required.
   * @return the created {@link DescopeList}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  DescopeList create(ListRequest request) throws DescopeException;

  /**
   * Update an existing list. Fields left null in the request are not sent.
   *
   * @param id the ID of the list to update
   * @param request the list's new details
   * @return the updated {@link DescopeList}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  DescopeList update(String id, ListRequest request) throws DescopeException;

  /**
   * Delete a list.
   *
   * @param id the ID of the list to delete
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void delete(String id) throws DescopeException;

  /**
   * Load a list by its ID.
   *
   * @param id the ID of the list
   * @return the {@link DescopeList}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  DescopeList load(String id) throws DescopeException;

  /**
   * Load a list by its name.
   *
   * @param name the name of the list
   * @return the {@link DescopeList}
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  DescopeList loadByName(String name) throws DescopeException;

  /**
   * Load all the lists in the project.
   *
   * @return the project's lists
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  List<DescopeList> loadAll() throws DescopeException;

  /**
   * Import lists into the project. Each list requires a name.
   *
   * @param lists the lists to import
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void importLists(List<DescopeList> lists) throws DescopeException;

  /**
   * Remove all the values from a list.
   *
   * @param id the ID of the list to clear
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void clear(String id) throws DescopeException;

  /**
   * Add IPs or CIDR ranges to an IPs list.
   *
   * @param id the ID of the list
   * @param ips the IPs to add, at least one
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void addIPs(String id, List<String> ips) throws DescopeException;

  /**
   * Remove IPs or CIDR ranges from an IPs list.
   *
   * @param id the ID of the list
   * @param ips the IPs to remove, at least one
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void removeIPs(String id, List<String> ips) throws DescopeException;

  /**
   * Check whether an IP is in an IPs list.
   *
   * @param id the ID of the list
   * @param ip the IP to check
   * @return true if the IP is in the list
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  boolean checkIP(String id, String ip) throws DescopeException;

  /**
   * Add texts to a texts list.
   *
   * @param id the ID of the list
   * @param texts the texts to add, at least one
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void addTexts(String id, List<String> texts) throws DescopeException;

  /**
   * Remove texts from a texts list.
   *
   * @param id the ID of the list
   * @param texts the texts to remove, at least one
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  void removeTexts(String id, List<String> texts) throws DescopeException;

  /**
   * Check whether a text is in a texts list.
   *
   * @param id the ID of the list
   * @param text the text to check
   * @return true if the text is in the list
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  boolean checkText(String id, String text) throws DescopeException;

  /**
   * Check whether several texts are in a texts list.
   *
   * @param id the ID of the list
   * @param texts the texts to check, up to 100
   * @param matchAll when true, the result is true only if every text is in the list, otherwise if any is
   * @return the result of the check
   * @throws DescopeException If there occurs any exception, a subtype of this exception will be thrown.
   */
  boolean checkTexts(String id, List<String> texts, boolean matchAll) throws DescopeException;
}
