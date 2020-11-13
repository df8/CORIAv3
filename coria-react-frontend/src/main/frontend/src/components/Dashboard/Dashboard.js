/**
 * Created by David Fradin, 2020
 */

import * as React from "react";
import {Card, CardContent, List, ListItem, ListItemText, ListItemAvatar, Avatar, Link} from '@material-ui/core';
import {makeStyles} from '@material-ui/core/styles';
import DescriptionIcon from '@material-ui/icons/Description';
import {Title} from 'react-admin';

const useStyles = makeStyles((theme) => ({
    secondaryTextSegment: {
        marginRight: theme.spacing(3),
    },
}));

const Dashboard = () => {
    const publications = [
        {
            title: "Internet Resilience and Connectivity: Risks for Online Businesses",
            authors: "Baumann, Annika",
            type: "Master’s thesis",
            institution: "Humboldt-Universität zu Berlin",
            date: "2013-03",
        },
        {
            title: "CORIA – Analyzing Internet Connectivity Risks Using Network Graphs",
            authors: "Fabian, B., Baumann, A., Ehlert, M., Ververis, V., & Ermakova, T.",
            type: "Article in the proceedings of a conference",
            date: "2017-05",
            url: "https://www.researchgate.net/publication/313083776_CORIA_-_Analyzing_Internet_Connectivity_Risks_Using_Network_Graphs",
            urlText: "ResearchGate",
            BibTeX: `@inproceedings{fabian2017coria,
  title={CORIA—Analyzing internet connectivity risks using network graphs},
  author={Fabian, Benjamin and Baumann, Annika and Ehlert, Mathias and Ververis, Vasilis and Ermakova, Tatiana},
  booktitle={2017 IEEE International Conference on Communications (ICC)},
  pages={1--6},
  year={2017},
  organization={IEEE}
}`
        },
        {
            title: "Development of a modular software framework for the analysis of network connectivity risks based on network graphs",
            authors: "Gross, Sebastian",
            type: "Bachelor’s thesis",
            institution: "Hochschule für Telekommunikation Leipzig (FH)",
            date: "2017-12",
        },
        {
            title: "Accelerating the Analysis of Network Connectivity Risks: Development of High-Performance Software Modules on the GPU",
            authors: "Fradin, David",
            type: "Master’s thesis",
            institution: "Humboldt-Universität zu Berlin",
            date: "2020-11",
        }
    ];
    const classes = useStyles();
    const preventDefault = (event) => event.preventDefault();
    return (
        <Card>
            <Title title="CORIA v3.1"/>
            <CardContent>
                <div className="page-header">
                    <h1>CORIA v3.1</h1>
                </div>
                <p><b>CORIA</b> is a software framework for an easy analysis of connectivity risks based on large
                    network graphs. It provides researchers, risk analysts, network managers and security consultants
                    with a tool to assess an organization’s connectivity and paths options through the Internet backbone,
                    including a user-friendly and insightful visual representation of results.</p>
                <p>The system is designed to analyse networks (or graphs) of any size. In the context of Autonomous Systems
                    (AS) we consider the Internet a network of interconnected routers where an AS can be represented by a node
                    and the physical link between any two AS as an edge. This allows us to transfer research problems such as
                    measuring global and regional Internet resilience into known and in some cases already solved mathematical
                    problems within the field of graph theory.</p>
                <h2>Related Academic Publications</h2>
                <List>
                    {publications.map((publication, index) => <ListItem key={index}>
                        <ListItemAvatar><Avatar><DescriptionIcon/></Avatar></ListItemAvatar>
                        <ListItemText primary={publication.title}
                                      secondary={<span>
                                          <span
                                              className={classes.secondaryTextSegment}><b>Published: </b>{publication.date}</span>
                                          <span
                                              className={classes.secondaryTextSegment}><b>Author(s): </b>{publication.authors}</span>
                                          {publication.url &&
                                          <Link className={classes.secondaryTextSegment}
                                                href={publication.url}
                                                onClick={preventDefault}
                                                target="_blank"
                                                rel="noreferrer">...more on {publication.urlText}</Link>}
                                      </span>}
                        />
                    </ListItem>)}
                </List>
            </CardContent>
        </Card>
    );
}

export default Dashboard;