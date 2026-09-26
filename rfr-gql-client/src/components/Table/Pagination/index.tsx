import {Box, Button} from "@mui/material"
import {FC} from "react"
import ChevronLeftRoundedIcon from '@mui/icons-material/ChevronLeftRounded';
import ChevronRightRoundedIcon from '@mui/icons-material/ChevronRightRounded';

interface TablePaginationInterface {
    hasNextValues: boolean;
    hasPreviousValues: boolean;
    onPreviousClick: () => void;
    onNextClick: () => void;
}

export const TablePagination: FC<TablePaginationInterface> = ({
                                                                  hasNextValues,
                                                                  hasPreviousValues,
                                                                  onPreviousClick,
                                                                  onNextClick
                                                              }) => {
    if (!hasNextValues && !hasPreviousValues) {
        return null;
    }
    return (
        <Box sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "flex-end",
            gap: 1,
            px: 2,
            py: 1.5,
            borderTop: 1,
            borderColor: "divider",
        }}>
            <Button type="button"
                    size="small"
                    startIcon={<ChevronLeftRoundedIcon/>}
                    disabled={!hasPreviousValues}
                    onClick={onPreviousClick}
            >
                Previous
            </Button>
            <Button type="button"
                    size="small"
                    endIcon={<ChevronRightRoundedIcon/>}
                    disabled={!hasNextValues}
                    onClick={onNextClick}
            >
                Next
            </Button>
        </Box>
    )
}
