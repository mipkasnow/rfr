import {Box, Card, IconButton, Tooltip} from "@mui/material";
import CloseFullscreenRoundedIcon from '@mui/icons-material/CloseFullscreenRounded';
import OpenInFullRoundedIcon from '@mui/icons-material/OpenInFullRounded';
import {FC, ReactNode, useLayoutEffect, useRef, useState} from "react";

const STORAGE_KEY = "rangiffler-map-collapsed";
const COLLAPSED_HEIGHT = 180;
const COLLAPSED_OFFSET_RATIO = 0.3;
const TRANSITION = "0.35s cubic-bezier(0.4, 0, 0.2, 1)";

interface CollapsibleMapCardInterface {
    children: ReactNode;
}

const readCollapsed = () => localStorage.getItem(STORAGE_KEY) === "true";

export const CollapsibleMapCard: FC<CollapsibleMapCardInterface> = ({children}) => {
    const [collapsed, setCollapsed] = useState(readCollapsed);
    const [contentHeight, setContentHeight] = useState<number | null>(null);
    const [animating, setAnimating] = useState(false);
    const contentRef = useRef<HTMLDivElement>(null);
    const clipRef = useRef<HTMLDivElement>(null);
    const scrollSyncRef = useRef(0);

    useLayoutEffect(() => {
        const content = contentRef.current;
        if (!content) {
            return;
        }
        const update = () => setContentHeight(content.offsetHeight);
        update();
        const observer = new ResizeObserver(update);
        observer.observe(content);
        return () => observer.disconnect();
    }, []);

    const syncScrollWithCollapse = (fromHeight: number, toHeight: number) => {
        const clip = clipRef.current;
        const shrink = fromHeight - toHeight;
        const startScroll = window.scrollY;
        const maxScrollAfter = document.documentElement.scrollHeight - shrink - window.innerHeight;
        const targetScroll = Math.max(0, Math.min(startScroll, maxScrollAfter));
        if (!clip || shrink <= 0 || targetScroll >= startScroll) {
            return;
        }
        const token = ++scrollSyncRef.current;
        const step = () => {
            if (token !== scrollSyncRef.current) {
                return;
            }
            const progress = Math.min(1, Math.max(0, (fromHeight - clip.getBoundingClientRect().height) / shrink));
            window.scrollTo(0, startScroll + (targetScroll - startScroll) * progress);
            if (progress < 1) {
                requestAnimationFrame(step);
            }
        };
        requestAnimationFrame(step);
    };

    const toggle = () => {
        scrollSyncRef.current++;
        if (!collapsed && contentHeight) {
            syncScrollWithCollapse(contentHeight, collapsedHeight);
        }
        setAnimating(true);
        setCollapsed((current) => {
            localStorage.setItem(STORAGE_KEY, String(!current));
            return !current;
        });
    };

    const fullHeight = contentHeight ?? undefined;
    const collapsedHeight = contentHeight ? Math.min(COLLAPSED_HEIGHT, contentHeight) : COLLAPSED_HEIGHT;
    const offset = collapsed && contentHeight ? Math.round(contentHeight * COLLAPSED_OFFSET_RATIO) : 0;

    return (
        <Card sx={{position: "relative", p: {xs: 1, md: 3}, mb: 2}}>
            <Box
                ref={clipRef}
                sx={{
                    overflow: "hidden",
                    height: collapsed ? collapsedHeight : fullHeight,
                    transition: animating ? `height ${TRANSITION}` : "none",
                    "@media (prefers-reduced-motion: reduce)": {transition: "none"},
                }}
                onTransitionEnd={(e) => e.target === e.currentTarget && setAnimating(false)}
            >
                <Box
                    ref={contentRef}
                    sx={{
                        transform: `translateY(-${offset}px)`,
                        transition: animating ? `transform ${TRANSITION}` : "none",
                        "@media (prefers-reduced-motion: reduce)": {transition: "none"},
                    }}
                >
                    {children}
                </Box>
            </Box>
            <Tooltip title={collapsed ? "Expand map" : "Collapse map"}>
                <IconButton
                    aria-label={collapsed ? "Expand map" : "Collapse map"}
                    aria-expanded={!collapsed}
                    onClick={toggle}
                    size="small"
                    sx={{
                        position: "absolute",
                        right: 12,
                        bottom: 12,
                        bgcolor: "background.paper",
                        border: 1,
                        borderColor: "divider",
                        boxShadow: 1,
                        "&:hover": {bgcolor: "background.paper", color: "primary.main"},
                    }}
                >
                    {collapsed ? <OpenInFullRoundedIcon fontSize="small"/> : <CloseFullscreenRoundedIcon fontSize="small"/>}
                </IconButton>
            </Tooltip>
        </Card>
    );
};
