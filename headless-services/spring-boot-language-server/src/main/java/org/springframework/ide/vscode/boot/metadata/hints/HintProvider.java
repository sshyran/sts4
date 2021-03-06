/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata.hints;

import java.util.List;

import org.springframework.ide.vscode.boot.metadata.types.TypedProperty;
import org.springframework.ide.vscode.commons.yaml.path.YamlNavigable;

/**
 * @author Kris De Volder
 */
public interface HintProvider extends YamlNavigable<HintProvider> {
	List<StsValueHint> getValueHints(String query);
	List<TypedProperty> getPropertyHints(String query);
}
