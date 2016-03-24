/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.sherpa.drawing.decorator;

import com.google.tnt.solver.widgets.ConstraintWidget;

/**
 * Switch Widget decorator
 */
public class SwitchWidget extends CheckboxWidget {

    /**
     * Base constructor
     *
     * @param widget the widget we are decorating
     * @param text   the text content
     */
    public SwitchWidget(ConstraintWidget widget, String text) {
        super(widget, text);
    }

    public String getImagePath() { return "/blueprint_radio_on.png"; }
}
