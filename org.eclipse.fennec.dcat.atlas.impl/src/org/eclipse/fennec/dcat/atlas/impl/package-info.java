/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
/**
 * File-backed implementations of the DCAT-AP entity services.
 * <p>
 * {@link org.eclipse.fennec.m2x.ocl.api.annotation.require.RequireOCL} because the write
 * boundary validates through EMF's {@code Diagnostician}, and the OCL invariants annotated
 * on the model are evaluated by a delegate the {@code m2x} OCL engine provides. Without
 * the engine present, EMF reports every annotated constraint as
 * {@code constraint delegate not found} at {@code ERROR} — so a missing engine does not
 * silently disable validation, it rejects every write. Declaring the requirement makes the
 * resolver bring the engine along instead of leaving that to be discovered at runtime.
 */
@org.eclipse.fennec.m2x.ocl.api.annotation.require.RequireOCL
package org.eclipse.fennec.dcat.atlas.impl;
