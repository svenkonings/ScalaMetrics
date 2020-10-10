import pandas as pd

from analysis import categories, projects, save_dataframe, get_metric_results, parse_args, get_columns, \
    split_paradigm_score
from analysis.summarise import summarise_directory, summarise_split_directory


def main(args):
    """
    Calculates statistics of the fault data per metric.
    """
    if args.split_paradigm_score:
        folder = f'{args.folder}/split-regression/fault-metric-statistics/'
    else:
        folder = f'{args.folder}/regression/fault-metric-statistics/'
    for category in categories:
        for path, name in projects.items():
            df = get_metric_results(args.folder, path, category)
            if df is not None:
                if args.split_paradigm_score:
                    for paradigm, scores in split_paradigm_score(df, args.folder, path, category):
                        fault_metric_statistics(folder, scores, category, name + paradigm, args)
                else:
                    fault_metric_statistics(folder, df, category, name, args)
    if args.split_paradigm_score:
        summarise_split_directory(args, 'fault-metric-statistics',
                                  ['name', 'percentage_faulty', 'percentage_total_faults'])
    else:
        summarise_directory(args, 'fault-metric-statistics', ['name', 'percentage_faulty', 'percentage_total_faults'])


def fault_metric_statistics(folder, df, category, name, args):
    print(f'[{category}] Fault-metric-statistics: {name}')
    faulty_rows = df[df['faults'] > 0]
    if faulty_rows.empty:
        print('No faulty rows -- skipping!')
        return
    statistics = pd.DataFrame(
        columns=['name', 'rows_with_data', 'faulty_rows_with_data', 'non_faulty_rows_with_data', 'percentage_faulty',
                 'percentage_total_faults']
    )
    columns = get_columns(df, args)
    for column in columns:
        column_rows = df[df[column] != 0]
        if not column_rows.empty:
            faulty_column_rows = df[(df[column] != 0) & (df['faults'] > 0)]
            non_faulty_column_rows = df[(df[column] != 0) & (df['faults'] == 0)]
            # When the metric has data, which percentage of code is faulty
            percentage_column_faulty = (len(faulty_column_rows) / len(column_rows)) * 100
            # When code is faulty, which percentage has data
            percentage_faulty_total = (len(faulty_column_rows) / len(faulty_rows)) * 100
            result = {
                'name': column,
                'rows_with_data': len(column_rows),
                'faulty_rows_with_data': len(faulty_column_rows),
                'non_faulty_rows_with_data': len(non_faulty_column_rows),
                'percentage_faulty': percentage_column_faulty,
                'percentage_total_faults': percentage_faulty_total
            }
            statistics = statistics.append(result, ignore_index=True)
    if not statistics.empty:
        save_dataframe(statistics, folder + category, name, False)


if __name__ == '__main__':
    main(parse_args())
