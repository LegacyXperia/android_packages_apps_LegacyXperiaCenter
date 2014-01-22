/*
 * Copyright (C) 2013 Slimroms http://www.slimroms.net
 * Copyright (C) 2014 LegacyXperia Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.legacyxperia.center;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class About extends Fragment {

    private LinearLayout website;
    private LinearLayout wiki;
    private LinearLayout changelog;
    private LinearLayout source;
    private LinearLayout donate;

    private final View.OnClickListener mActionLayouts = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == website) {
                launchUrl("http://legacyxperia.github.io");
            } else if (v == wiki) {
                launchUrl("https://github.com/LegacyXperia/Wiki/wiki");
            } else if (v == changelog) {
                launchUrl("https://github.com/LegacyXperia/local_manifests/wiki/Changelog");
            } else if (v == source) {
                launchUrl("https://github.com/LegacyXperia");
            } else if (v == donate) {
                launchUrl("http://forum.xda-developers.com/donatetome.php?u=3839575");
            }
        }
    };

    private void launchUrl(String url) {
        final Uri uriUrl = Uri.parse(url);
        final Intent openUrl = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(openUrl);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        website = (LinearLayout) getView().findViewById(R.id.lx_website);
        website.setOnClickListener(mActionLayouts);

        wiki = (LinearLayout) getView().findViewById(R.id.lx_wiki);
        wiki.setOnClickListener(mActionLayouts);

        changelog = (LinearLayout) getView().findViewById(R.id.lx_changelog);
        changelog.setOnClickListener(mActionLayouts);

        source = (LinearLayout) getView().findViewById(R.id.lx_source);
        source.setOnClickListener(mActionLayouts);

        donate = (LinearLayout) getView().findViewById(R.id.lx_donate);
        donate.setOnClickListener(mActionLayouts);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.lx_about, container,
                false);
        return view;
    }
}
