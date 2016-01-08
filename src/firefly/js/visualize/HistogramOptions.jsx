import React from 'react';

import {flux} from '../Firefly.js';


//import PureRenderMixin from 'react-addons-pure-render-mixin';
//import ReactDOM from 'react-dom';

import {ColValuesStatistics} from './ColValuesStatistics.js';
import CompleteButton from '../ui/CompleteButton.jsx';
import FieldGroup from '../ui/FieldGroup.jsx';
import FieldGroupUtils from '../fieldGroup/FieldGroupUtils.js';
import InputGroup from '../ui/InputGroup.jsx';
import Validate from '../util/Validate.js';
import ValidationField from '../ui/ValidationField.jsx';
import RadioGroupInputField from '../ui/RadioGroupInputField.jsx';
import ListBoxInputField from '../ui/ListBoxInputField.jsx';



var HistogramOptions = React.createClass({

    storeListenerRemove : null,

    propTypes: {
        groupKey: React.PropTypes.string.isRequired,
        colValStats: React.PropTypes.arrayOf(React.PropTypes.instanceOf(ColValuesStatistics)).isRequired,
        onOptionsSelected: React.PropTypes.func.isRequired
    },

    /*
      @param histogramParams key value pairs
      React.PropTypes.shape({
         algorithm : React.PropTypes.oneOf(['fixedSizeBins','byesianBlocks']),
         numBins : React.PropTypes.number,
         falsePositiveRate : React.PropTypes.number,
         minCutoff : React.PropTypes.number,
         maxCutoff : React.PropTypes.number
      })
     */

    getInitialState() {
        return {fields : FieldGroupUtils.getGroupFields(this.props.groupKey)};
    },

    componentWillUnmount() {
        if (this.storeListenerRemove) this.storeListenerRemove();
    },


    componentDidMount() {
        this.storeListenerRemove= flux.addListener(this.storeUpdate);
    },

    storeUpdate() {
        const newFields = FieldGroupUtils.getGroupFields(this.props.groupKey);
        if (this.state.fields !== newFields) {
            this.setState({fields: newFields});
        }
    },

    resultsSuccess(histogramParams) {
        this.props.onOptionsSelected(histogramParams);
    },

    resultsFail() {
        // TODO: do I need to do anything here?
    },

    renderAlgorithmParameters() {
        const {groupKey} = this.props;
        const {fields} = this.state;

        var algorithm =  (fields && fields.algorithm) ? fields.algorithm.value : 'fixedSizeBins';

        if (algorithm == 'byesianBlocks') {
            const val =  (fields && fields.falsePositiveRate) ? fields.falsePositiveRate.value : 0.05;
            return (
                <div>
                <ValidationField
                    style={{width: 30}}
                    initialState= {{
                        value: val,
                        validator: Validate.floatRange.bind(null, 0.01, 0.5, 2,'falsePositiveRate'),
                        tooltip: 'Acceptable false positive rate',
                        label : 'False Positive Rate:',
                        labelWidth : 200
                    }}
                    fieldKey='falsePositiveRate'
                    groupKey={groupKey}
                />
                </div>
            );
        } else { // fixedSizeBins
            const val =  (fields && fields.numBins) ? fields.numBins.value : 5;
            return (
                <ValidationField
                    style={{width: 30}}
                    initialState= {{
                        value: val,
                        validator: Validate.intRange.bind(null, 1, 500, 'numBins'),
                        tooltip: 'Number of fixed size bins',
                        label : 'Number of bins:',
                        labelWidth : 150
                    }}
                    fieldKey='numBins'
                    groupKey={groupKey}
                />
            );
        }
    },

    render() {
        const { colValStats, groupKey }= this.props;
        const {fields} = this.state;
        return (
            <div style={{padding:'5px'}}>

                <FieldGroup groupKey={groupKey} validatorFunc={null} keepState={true}>
                    <ListBoxInputField
                        initialState= {{
                                tooltip: 'Please select a column',
                                label : 'Column or expression:',
                                labelWidth : 200
                            }}
                        options={
                                colValStats.map((colVal) => {
                                    return {
                                        label: colVal.name + ' ' + (colVal.unit && colVal.unit !== 'null' ? colVal.unit : ''),
                                        value: colVal.name
                                    };
                                })
                            }
                        multiple={false}
                        fieldKey='columnOrExpr'
                        groupKey={groupKey}/>
                    <br/><br/>
                    <InputGroup labelWidth={100}>
                        <RadioGroupInputField
                            initialState= {{
                                value : (fields && fields.algorithm) ? fields.algorithm.value : 'fixedSizeBins',
                                tooltip: 'Please select an algorithm',
                                label: 'Algorithm:'
                            }}
                            options={
                            [
                                {label: 'Byesian blocks', value: 'byesianBlocks'},
                                {label: 'Fixed size', value: 'fixedSizeBins'}
                            ]
                        }
                            alignment='horizontal'
                            fieldKey='algorithm'
                            groupKey={groupKey}/>
                    </InputGroup>
                    <br/>
                    {this.renderAlgorithmParameters()}
                    <br/><br/>
                    <CompleteButton groupKey={groupKey}
                                    onSuccess={this.resultsSuccess}
                                    onFail={this.resultsFail}
                        />
                </FieldGroup>

            </div>
        );
    }
});

export default HistogramOptions;