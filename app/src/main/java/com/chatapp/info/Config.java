package com.chatapp.info;

import com.airbnb.epoxy.EpoxyDataBindingPattern;

@EpoxyDataBindingPattern(rClass = R.class, layoutPrefix = "item")
interface Config {}


/** here inside the layout prefix it must be layouts
 * that provide by epoxy start with the name of prefix that you will write it **/