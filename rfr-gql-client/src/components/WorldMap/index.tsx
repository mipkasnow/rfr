import {Box} from "@mui/material";
import {FC, useLayoutEffect, useRef, useState} from 'react';
import SvgWorldMap, {ISOCode} from 'react-svg-worldmap';
import {Stat} from "../../types/Stat";
import "./styles.css";

const MAP_HEIGHT_RATIO = 3 / 4;
const RESERVED_VERTICAL_SPACE = 180;
const MIN_WIDTH = 240;

interface WorldMapInterface {
    data: Stat[],
    selectedCountry?: string | null,
    onCountryClick?: (countryCode: string) => void,
}

const maxWidthForViewport = () => (window.innerHeight - RESERVED_VERTICAL_SPACE) / MAP_HEIGHT_RATIO;

const useMapWidth = () => {
    const containerRef = useRef<HTMLDivElement>(null);
    const [width, setWidth] = useState<number | null>(null);

    useLayoutEffect(() => {
        const container = containerRef.current;
        if (!container) {
            return;
        }
        const update = () => setWidth(Math.floor(Math.min(container.clientWidth, Math.max(MIN_WIDTH, maxWidthForViewport()))));
        update();
        const observer = new ResizeObserver(update);
        observer.observe(container);
        window.addEventListener("resize", update);
        return () => {
            observer.disconnect();
            window.removeEventListener("resize", update);
        };
    }, []);

    return {containerRef, width};
};

export const WorldMap: FC<WorldMapInterface> = ({data = [], selectedCountry, onCountryClick}) => {
    const {containerRef, width} = useMapWidth();

    const mapData = data.map((v) => ({
        country: v.country.code as ISOCode,
        value: v.count,
    }));

    return (
        <Box ref={containerRef} sx={{width: "100%"}}>
            {width && (
                <SvgWorldMap
                    size={width}
                    data={mapData}
                    valueSuffix="photos"
                    backgroundColor="transparent"
                    richInteraction={true}
                    onClickFunction={({countryCode, countryValue}) => {
                        if (countryValue !== undefined) {
                            onCountryClick?.(countryCode.toLowerCase());
                        }
                    }}
                    styleFunction={({countryCode, countryValue, minValue, maxValue}) => {
                        const visited = countryValue !== undefined;
                        const selected = selectedCountry === countryCode.toLowerCase();
                        const dimmed = Boolean(selectedCountry) && !selected;
                        const intensity = visited ? (countryValue - minValue) / Math.max(1, maxValue - minValue) : 0;
                        const fill = selected
                            ? "var(--mui-palette-warning-main)"
                            : visited ? "var(--mui-palette-primary-main)" : "var(--mui-palette-text-primary)";
                        return {
                            fill,
                            fillOpacity: selected ? 1 : !visited ? 0.07 : dimmed ? 0.35 : 0.75 + 0.25 * intensity,
                            stroke: "var(--mui-palette-background-paper)",
                            strokeWidth: 0.6,
                            outline: "none",
                            cursor: visited ? "pointer" : "default",
                        };
                    }}
                />
            )}
        </Box>
    );
}
