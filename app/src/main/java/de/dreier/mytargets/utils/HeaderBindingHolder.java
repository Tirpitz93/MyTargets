package de.dreier.mytargets.utils;

import android.support.annotation.IdRes;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public abstract class HeaderBindingHolder<T> extends ItemBindingHolder<T> {

    private View expandCollapseView = null;
    private View.OnClickListener expandListener;
    private boolean expanded = false;

    /**
     * Constructor for header items
     *
     * @param itemView        Header view
     * @param expand_collapse Expand/Collapse ImageView's resource id
     */
    public HeaderBindingHolder(View itemView, @IdRes int expand_collapse) {
        super(itemView);

        itemView.setOnClickListener(this);

        expandCollapseView = itemView.findViewById(expand_collapse);
    }

    @Override
    public void onClick(View v) {
        if (expandListener != null) {
            expandListener.onClick(v);
            expandCollapseView.animate()
                    .rotation(expanded ? 0 : 180)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
            expanded = !expanded;
        }
    }

    /**
     * Returns whether {@link #itemView} is currently in a
     * selectable mode.
     *
     * @return True if selectable.
     */
    public boolean isSelectable() {
        return false;
    }

    /**
     * Does nothing.
     *
     * @param isSelectable True if selectable.
     */
    public void setSelectable(boolean isSelectable) {
    }

    @Override
    protected void onRebind() {
    }

    public void setExpandOnClickListener(View.OnClickListener onClickListener, boolean expanded) {
        expandListener = onClickListener;
        this.expanded = expanded;
        expandCollapseView.setRotation(expanded ? 180 : 0);
    }

    /**
     * Calls through to {@link #itemView#isActivated}.
     *
     * @return True if the view is activated.
     */
    public boolean isActivated() {
        return itemView.isActivated();
    }

    /**
     * Calls through to {@link #itemView#setActivated}.
     *
     * @param isActivated True to activate the view.
     */
    public void setActivated(boolean isActivated) {
        itemView.setActivated(isActivated);
    }
}
